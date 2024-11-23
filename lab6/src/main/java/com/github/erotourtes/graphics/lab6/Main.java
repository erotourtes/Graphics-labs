package com.github.erotourtes.graphics.lab6;

import org.joml.Vector3f;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private final Vector3f cubePosition = new Vector3f(0.0f, 0.0f, 0.0f);

    public void run() {
        init();
        loop();

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = glfwCreateWindow(1800, 1200, "Lab6", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, this::handleKeyboardInput);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
    }

    private void handleKeyboardInput(long window, int key, int scancode, int action, int mods) {
        float moveSpeed = 0.1f;

        if (action == GLFW_PRESS || action == GLFW_REPEAT) {
            switch (key) {
                case GLFW_KEY_W -> cubePosition.z += moveSpeed; // Move forward
                case GLFW_KEY_S -> cubePosition.z -= moveSpeed; // Move backward
                case GLFW_KEY_A -> cubePosition.x -= moveSpeed; // Move left
                case GLFW_KEY_D -> cubePosition.x += moveSpeed; // Move right
                case GLFW_KEY_Q -> cubePosition.y += moveSpeed; // Move up
                case GLFW_KEY_E -> cubePosition.y -= moveSpeed; // Move down
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true); // Exit
            }
        }
    }

    private void loop() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glClearColor(0.1f, 0.1f, 0.1f, 0.0f);
        setupProjection();

        float angle = 0.0f;

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glPushMatrix();
            glTranslatef(cubePosition.x, cubePosition.y, cubePosition.z);
            glTranslatef(0.0f, 0.0f, -5.0f); // Move cube away from the camera
            glRotatef(angle, 1.0f, 1.0f, 0.0f); // Rotate the cube on X and Y axes

            drawCube();

            glPopMatrix();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();

            angle += 0.7f;
        }
    }

    private void drawCube() {
        glBegin(GL_QUADS);

        // Front face
        glColor3f(1.0f, 0.0f, 0.0f); // Red
        glVertex3f(-1.0f, -1.0f, 1.0f);
        glVertex3f(1.0f, -1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 1.0f);
        glVertex3f(-1.0f, 1.0f, 1.0f);

        // Back face
        glColor3f(0.0f, 1.0f, 0.0f); // Green
        glVertex3f(-1.0f, -1.0f, -1.0f);
        glVertex3f(-1.0f, 1.0f, -1.0f);
        glVertex3f(1.0f, 1.0f, -1.0f);
        glVertex3f(1.0f, -1.0f, -1.0f);

        // Top face
        glColor3f(0.0f, 0.0f, 1.0f); // Blue
        glVertex3f(-1.0f, 1.0f, -1.0f);
        glVertex3f(-1.0f, 1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, 1.0f);
        glVertex3f(1.0f, 1.0f, -1.0f);

        // Bottom face
        glColor3f(1.0f, 1.0f, 0.0f); // Yellow
        glVertex3f(-1.0f, -1.0f, -1.0f);
        glVertex3f(1.0f, -1.0f, 1.0f);
        glVertex3f(1.0f, -1.0f, 1.0f);
        glVertex3f(-1.0f, -1.0f, -1.0f);

        // Right face
        glColor3f(1.0f, 0.0f, 1.0f); // Magenta
        glVertex3f(1.0f, -1.0f, -1.0f);
        glVertex3f(1.0f, 1.0f, -1.0f);
        glVertex3f(1.0f, 1.0f, 1.0f);
        glVertex3f(1.0f, -1.0f, 1.0f);

        // Left face
        glColor3f(0.0f, 1.0f, 1.0f); // Cyan
        glVertex3f(-1.0f, -1.0f, -1.0f);
        glVertex3f(-1.0f, -1.0f, 1.0f);
        glVertex3f(-1.0f, 1.0f, 1.0f);
        glVertex3f(-1.0f, 1.0f, -1.0f);

        glEnd();
    }

    private void setupProjection() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        float fov = 80.0f;
        float aspect = 1800 / 1200f; // Replace with actual window size if needed
        float near = 0.1f;
        float far = 100.0f;

        float top = (float) (Math.tan(Math.toRadians(fov / 2)) * near);
        float bottom = -top;
        float right = top * aspect;
        float left = -right;

        glFrustum(left, right, bottom, top, near, far);

        glMatrixMode(GL_MODELVIEW);
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
