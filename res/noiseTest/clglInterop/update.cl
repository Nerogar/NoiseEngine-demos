__kernel void update(__global float2* data, float2 gravityCenter, float gravity){
	unsigned int id = get_global_id(0);

	// read data
	float2 oldPos = data[id * 2];
	float2 oldVel = data[id * 2 + 1];

	float2 velocity = oldVel;

	// calculate new velocity
	float2 delta = gravityCenter - oldPos;
	float invDist = rsqrt(delta.x*delta.x + delta.y*delta.y);

	velocity += ((delta * invDist) * min(invDist, 200.0f) * 0.01f * gravity);

	// friction
	velocity *= 0.97f;

	// update position
	float2 position = oldPos + oldVel * 0.01f;

	// keep in bounds
	float2 i;
	position = fract(position, &i);

	// write back
	data[id * 2]     = position;
	data[id * 2 + 1] = velocity;
}
