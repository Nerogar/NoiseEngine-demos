#version 330

layout (location = 0) out vec4 colorOut;

in vec2 vertexPosition;

void main() {
	colorOut = vec4(vertexPosition, 0.0, 1.0);
}
