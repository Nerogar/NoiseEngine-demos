#version 330

layout (location = 0) out vec4 colorOut;

in vec3 vertexColor;

void main() {
	colorOut = vec4(vertexColor, 1.0);
}
