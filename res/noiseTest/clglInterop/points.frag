#version 330

layout (location = 0) out vec4 colorOut;

in vec2 vPosition;
in vec2 vVelocity;

void main() {
	colorOut = vec4(0.5 * abs(vVelocity.xy), 0.2 * length(vPosition - vec2(0.5)), 1.0);
}
