#version 330 core

uniform mat4 projectionMatrix;

layout (location = 0) in vec2 position;
layout (location = 1) in vec3 color;

out vec3 vertexColor;

void main() {
	gl_Position = projectionMatrix * vec4(position, 0.0, 1.0);
	vertexColor = color;
}
