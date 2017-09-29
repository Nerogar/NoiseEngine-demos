#version 330 core

uniform mat4 projectionMatrix;

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 offset;

out vec2 vertexPosition;

void main() {
	gl_Position = projectionMatrix * vec4(position + offset, 0.0, 1.0);

	vertexPosition = position;
}
