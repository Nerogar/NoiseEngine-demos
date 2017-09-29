/*double rand(int* seed){ // 1 <= *seed < m
	const int a = 16807; //ie 7**5
	const int m = 2147483647; //ie 2**31-1

	*seed = (convert_long(*seed) * a) % m;
	long low = (*seed);
	*seed = (convert_long(*seed) * a) % m;
	long high = convert_long(*seed) << 31;

	return convert_double((high|low) & (0x1FFFFFFFFFFFFF)) * 0x1.0p-53;
}*/

float rand(int* seed1){ // 1 <= *seed < m
	const int a = 16807; //ie 7**5
	const int m = 2147483647; //ie 2**31-1

	*seed1 = (convert_long(*seed1) * a) % m;
	int first = (*seed1);

	float f1 = convert_float(first & (0x7FFFFF)) * 0x1.0p-23; //random number in [0, 1]
	return f1;
}


float2 rand2(int* seed1, int* seed2){ // 1 <= *seed < m
	const int a = 16807; //ie 7**5
	const int m = 2147483647; //ie 2**31-1

	//if seed2 did a complete cycle, throw 1 number away to offset seed1 and seed2
	//if(*seed2 == 1){
	//	*seed2 = (convert_long(*seed2) * a) % m;
	//}

	*seed1 = (convert_long(*seed1) * a) % m;
	int first = (*seed1);
	*seed2 = (convert_long(*seed2) * a) % m;
	int second = (*seed2);

	float f1 = convert_float(first & (0x1FFFFFF)) * 0x1.0p-23; //random number in [0, 2]
	float f2 = convert_float(second & (0x1FFFFFF)) * 0x1.0p-23; //random number in [0, 2]
	return (float2)(f1, f2);
}

float2 randComplex(int* seed1, int* seed2){ // 1 <= *seed < m
	const int a = 16807; //ie 7**5
	const int m = 2147483647; //ie 2**31-1

	//if seed2 did a complete cycle, throw 1 number away to offset seed1 and seed2
	//if(*seed2 == 1){
	//	*seed2 = (convert_long(*seed2) * a) % m;
	//}

	*seed1 = (convert_long(*seed1) * a) % m;
	int first = (*seed1);
	*seed2 = (convert_long(*seed2) * a) % m;
	int second = (*seed2);

	float f1 = convert_float(first & (0x1FFFFFF)) * 0x1.0p-23; //random number in [0, 4]
	f1 -= 2.0;
	float f2 = convert_float(second & (0x1FFFFFF)) * 0x1.0p-23; //random number in [0, 4]
	f2 -= 2.0;
	return (float2)(f1, f2);
}

#define WIDTH        2048
#define HEIGHT       1024
#define MAP_WIDTH    512
#define MAP_HEIGHT   512



float2 texToCompl(int2 coord, float2 low, float2 high){
	float re = ((float) coord.x / WIDTH) * (high.x - low.x) + low.x;
	float im = ((float) coord.y / HEIGHT) * (high.y - low.y) + low.y;
	
	return (float2)(re, im);
}

int2 complToTex(float2 complex, float2 low, float2 high){
	int x = (int) (((complex.x - low.x) / (high.x - low.x)) * WIDTH);
	int y = (int) (((complex.y - low.y) / (high.y - low.y)) * HEIGHT);
	
	return (int2)(x, y);
}

float2 mapToCompl(int2 coord, float2 low, float2 high){
	float re = ((float) coord.x / MAP_WIDTH) * (high.x - low.x) + low.x;
	float im = ((float) coord.y / MAP_HEIGHT) * (high.y - low.y) + low.y;
	
	return (float2)(re, im);
}

int2 complToMap(float2 complex, float2 low, float2 high){
	int x = (int) (((complex.x - low.x) / (high.x - low.x)) * MAP_WIDTH);
	int y = (int) (((complex.y - low.y) / (high.y - low.y)) * MAP_HEIGHT);
	
	return (int2)(x, y);
}

float2 project(float reC, float imC, float reZ, float imZ, float4 projection){
	//return (float2)(reZ, imZ);
	return (float2)(reZ * projection.x + imC * projection.y, imZ * projection.x + reC * projection.y);
}

int search(__global float* data, int width, float sample){
	int left = 0;
	int right = width - 1;
	
	/*while(right > left + 1){
		int center = (left + right) / 2;
		if(sample < data[center]){
			left = center;
		}else{
			right = center;
		}
	}*/
	
	while(right > left){
		int center = (left + right) / 2;
		if(sample < data[center]){
			right = center;
		}else{
			left = center + 1;
		}
	}
	
	return left;
}

void buddhaBrot(__global int* data, __global int* prop, float reC, float imC, float2 low, float2 high, int maxIterations, float mult, float4 projection){
	float re = reC;
	float im = imC;
	
	int iterations = -1;
	for(int i = 0; i < maxIterations; i++){
		float re_new = re;
		float im_new = im;

		re = re_new * re_new - im_new * im_new + reC;
		im = re_new * im_new * 2 + imC;
		
		if(re*re + im*im > 1000000){
			iterations = i;
			break;
		}
	}

	re = reC;
	im = imC;
	
	int hits = 0;
	for(int i = 0; i < iterations; i++){
		float re_new = re;
		float im_new = im;

		re = re_new * re_new - im_new * im_new + reC;
		im = re_new * im_new * 2 + imC;
		
		if(re*re + im*im > 1000000) break;
		
		int2 coords = complToTex(project(reC, imC, re, im, projection), low, high);
		if(coords.x >= 0 && coords.y >= 0 && coords.x < WIDTH && coords.y < HEIGHT){
			atomic_add(&data[coords.y * WIDTH + coords.x], mult * 100000);
			hits++;
		}
	}
	int2 coords = complToMap((float2)(reC, imC), -2, 2);
	atomic_add(&prop[coords.y * MAP_WIDTH + coords.x], hits);
}

__kernel void buddhaBrotNaive(__global int* data, __global int* prop, __global float* propSample, __global int* random1, __global int* random2, float2 low, float2 high, int iterations, float4 projection){
	unsigned int id = get_global_id(0);

	int seed1 = random1[id];
	int seed2 = random2[id];

	float2 c = randComplex(&seed1, &seed2);
	buddhaBrot(data, prop, c.x, c.y, low, high, iterations, 1.0f, projection);

	random1[id] = seed1;
	random2[id] = seed2;
}

__kernel void buddhaBrotImportance(__global int* data, __global int* prop, __global float* propSample, __global int* random1, __global int* random2, float2 low, float2 high, int iterations, float4 projection){
	unsigned int id = get_global_id(0);

	int seed1 = random1[id];
	int seed2 = random2[id];

	float row = rand(&seed1) * 4 - 2;
	int rowIndex = complToMap((float2)(0, row), (float2)(-2, -2), (float2)(2, 2)).y;

	float column = rand(&seed2);
	__global float* rowPropSample = propSample + MAP_WIDTH * rowIndex;
	int columnIndex = search(rowPropSample, MAP_WIDTH, column);
	float currentProp = 0.0f;
	if(columnIndex > 0){
		currentProp = rowPropSample[columnIndex] - rowPropSample[columnIndex - 1];
	}else{
		currentProp = rowPropSample[columnIndex];
	}
	
	float2 c = mapToCompl((int2)(columnIndex, rowIndex), (float2)(-2, -2), (float2)(2, 2)) + rand2(&seed1, &seed2) * (float2)(1.0f / MAP_WIDTH, 1.0f / MAP_HEIGHT);	
	float mult = (1.0f / currentProp) / MAP_WIDTH;
	//mult = 1;
	buddhaBrot(data, prop, c.x, c.y, low, high, iterations, mult, projection);
	//atomic_inc(&data[rowIndex * MAP_WIDTH + columnIndex]);	
	//atomic_add(&data[rowIndex * MAP_WIDTH + 50], 1000);
	
	random1[id] = seed1;
	random2[id] = seed2;
}

__kernel void copyTexture(__write_only image2d_t texture, __global int* data, float brightness){
	unsigned int idx = get_global_id(0);
	unsigned int idy = get_global_id(1);
	int2 coords = (int2)(idx, idy);

	int count = data[coords.y * WIDTH + coords.x];
	write_imagef(texture, coords, (float4)(1.0, 0.3, 0.1, 0.0) * brightness * count);
	//write_imagef(texture, coords, (float4)(0.3, 1.0, 0.1, 0.0) * brightness * count);
}

__kernel void copyMap(__write_only image2d_t texture, __global int* data, float brightness){
	unsigned int idx = get_global_id(0);
	unsigned int idy = get_global_id(1);
	int2 coords = (int2)(idx * (MAP_WIDTH / WIDTH), idy * (MAP_HEIGHT / HEIGHT));
	int2 imageCoords = (int2)(idx, idy);

	int count = data[coords.y * MAP_WIDTH + coords.x];
	write_imagef(texture, imageCoords, (float4)(1.0, 0.3, 0.1, 0.0) * brightness * count);
}

__kernel void clearData(__global int* data, int value){
	unsigned int idx = get_global_id(0);
	unsigned int idy = get_global_id(1);
	int2 coords = (int2)(idx, idy);

	data[coords.y * WIDTH + coords.x] = value;
}

__kernel void clearMap(__global int* data, int value){
	unsigned int idx = get_global_id(0);
	unsigned int idy = get_global_id(1);
	int2 coords = (int2)(idx, idy);

	data[coords.y * MAP_WIDTH + coords.x] = value;
}
