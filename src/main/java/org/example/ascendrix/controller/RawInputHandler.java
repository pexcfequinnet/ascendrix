package org.example.ascendrix.controller;

import javafx.stage.Window;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class RawInputHandler {

    private RawInputHandler() {}

    /**
     * Call at the very start of main() before Application.launch()
     * so Linux GTK/X11 skips IBus/Fcitx IM initialization for this process.
     */
    public static void disableLinuxImeEnvironment() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("linux")) return;

        try {
            Linker linker = Linker.nativeLinker();
            SymbolLookup stdlib = linker.defaultLookup();
            MethodHandle setenv = linker.downcallHandle(
                    stdlib.find("setenv").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT)
            );

            try (Arena arena = Arena.ofConfined()) {
                setEnvVar(setenv, arena, "GTK_IM_MODULE", "none");
                setEnvVar(setenv, arena, "QT_IM_MODULE", "none");
                setEnvVar(setenv, arena, "XMODIFIERS", "@im=none");
            }
        } catch (Throwable ignored) {
            // Safe fallback if native access flag is not passed
        }
    }

    private static void setEnvVar(MethodHandle setenv, Arena arena, String key, String value) throws Throwable {
        MemorySegment k = arena.allocateFrom(key, StandardCharsets.UTF_8);
        MemorySegment v = arena.allocateFrom(value, StandardCharsets.UTF_8);
        int ignored = (int) setenv.invokeExact(k, v, 1);
    }

    /**
     * Detaches the OS IME context from the active JavaFX Stage.
     * On Windows: calls ImmAssociateContext(hwnd, NULL) so IMEs (Unikey, MS IME, etc.)
     * cannot swallow keys or turn them into VK_PROCESSKEY.
     */
    public static void enableForWindow(Window window) {
        if (window == null) return;
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (!os.contains("win")) return;

        try {
            Linker linker = Linker.nativeLinker();
            SymbolLookup user32 = SymbolLookup.libraryLookup("user32", Arena.global());
            SymbolLookup imm32 = SymbolLookup.libraryLookup("imm32", Arena.global());

            MethodHandle getActiveWindow = linker.downcallHandle(
                    user32.find("GetActiveWindow").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS)
            );
            MethodHandle immAssociateContext = linker.downcallHandle(
                    imm32.find("ImmAssociateContext").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS)
            );

            MemorySegment hwnd = (MemorySegment) getActiveWindow.invokeExact();
            if (hwnd != null && !hwnd.equals(MemorySegment.NULL)) {
                // Passing NULL as HIMC completely disables IME for this window handle
                MemorySegment ignored = (MemorySegment) immAssociateContext.invokeExact(hwnd, MemorySegment.NULL);
            }
        } catch (Throwable ignored) {
            // Safe fallback if --enable-native-access=ALL-UNNAMED is omitted
        }
    }
}