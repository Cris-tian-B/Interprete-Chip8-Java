/*
* Autor: Cris-tian-B
* Fecha: 9/26/2025
* Interprete Chip-8
* v0.9.0
*/

package com.micompany.test;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.glfw.Callbacks.*;
import java.nio.ByteBuffer;

public class Display {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 32;
    private static final int SCALE = 10; // Escala de visualización de la ventana
    private static final int WINDOW_WIDTH = WIDTH * SCALE;
    private static final int WINDOW_HEIGHT = HEIGHT * SCALE;
    private static long window;
    private static int textureID;
    private static ByteBuffer pixelBuffer; // Buffer RGBA para OpenGL.
    boolean draw = false; // Variable de condición para el bucle.
    static int[] keyMap = new int[] {
            88, 49, 50, 51,
            81, 87, 69, 65,
            83, 68, 90, 67,
            52, 82, 70, 86
    };
    static byte[] keyMapChip8 = new byte[16];
    /*
     *      1 2 3 C
     *      4 5 6 D
     *      7 8 9 E
     *      A 0 B F
     */
    public static void main(String[] args) {
        new Chip8();
        initGLFW();
        loop();
        cleanup();
    }

    public static void initGLFW() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configurar ventana
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Interprete CHIP-8 Java", NULL, NULL);

        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        // Configurar viewport
        glViewport(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f); // Fondo negro

        // Crear texture buffer
        pixelBuffer = BufferUtils.createByteBuffer(WIDTH * HEIGHT * 4); // RGBA
        createTexture();
    }

    private static void createTexture() {
        textureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, WIDTH, HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);
    }

    private static void drawPixel(int index, int on) {
        if (index < 0 || index >= WIDTH * HEIGHT)
            return;

        int position = index * 4;
        byte value = (byte) ((on == 1) ? 255 : 0);

        pixelBuffer.put(position, value); // R
        pixelBuffer.put(position + 1, value); // G
        pixelBuffer.put(position + 2, value); // B
        pixelBuffer.put(position + 3, (byte) 255); // A (opaco)
    }

    private static void clearPixels() {
        for (int i = 0; i < WIDTH * HEIGHT * 4; i += 4) {
            pixelBuffer.put(i, (byte) 0); // R
            pixelBuffer.put(i + 1, (byte) 0); // G
            pixelBuffer.put(i + 2, (byte) 0); // B
            pixelBuffer.put(i + 3, (byte) 255); // A
        }
    }

    private static void updateTexture() {
        glBindTexture(GL_TEXTURE_2D, textureID);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, WIDTH, HEIGHT, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);
    }

    private static void loop() {
        while (!glfwWindowShouldClose(window)) {
            processInput();
            Chip8.cycleCpu(); // Inicializar el ciclo de la CPU.

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // Dibujar texture
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, textureID);

            glBegin(GL_QUADS);
            glTexCoord2f(0, 1);
            glVertex2f(-1, -1);
            glTexCoord2f(1, 1);
            glVertex2f(1, -1);
            glTexCoord2f(1, 0);
            glVertex2f(1, 1);
            glTexCoord2f(0, 0);
            glVertex2f(-1, 1);
            glEnd();

            glfwSwapBuffers(window);
            glfwPollEvents();

            clearPixels();

            for (int i = 0; i < WIDTH * HEIGHT; i++) {
                drawPixel(i, Chip8.getPixel(i));
            }

            updateTexture();

            try {
                Thread.sleep(8); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static void processInput() {
        for (int i = 0; i < 16; i++) {
            if(isKeyPressed(keyMap[i], i)){
                //System.out.println("Tecla "+keyMap[i]+" presionada");
                keyMapChip8[i] = 1;
                Chip8.setKeyValue(i,1);
            }
            if(isKeyReleased(keyMap[i], i)){
                //System.out.println("Tecla "+keyMap[i]+" liberada");
                keyMapChip8[i] = 0;
                Chip8.setKeyValue(i,0);
            }
        }
    }

    private static boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    private static boolean isKeyPressed(int key, int pos) {
        return isKeyDown(key) && (keyMapChip8[pos] == 0);
    }

    private static boolean isKeyReleased(int key, int pos) {
        return !isKeyDown(key) && (keyMapChip8[pos] == 1);
    }

    private static void cleanup() {
        glDeleteTextures(textureID);
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
}
