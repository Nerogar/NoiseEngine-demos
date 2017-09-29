#version 330 core

uniform mat4 projectionMatrix;

layout (location = 0) in vec2 position;
layout (location = 1) in vec2 velocity;

out vec2 vPosition;
out vec2 vVelocity;

void main() {
	gl_Position = projectionMatrix * vec4(position, 0.0, 1.0);
	vPosition = position;
	vVelocity = velocity;
}
