__kernel void lineSum(__global int* data, __global int* sum, int width){
	int id = get_global_id(0);
	
	sum[id * width] = data[id * width]; 
	for(int i = 1; i < width; i++){
		sum[id * width + i] = data[id * width + i] + sum[id * width + i - 1]; 
	}
}

__kernel void lineNorm(__global int* sum, int width){
	int id = get_global_id(0);
	int maxId = ((id / width) + 1) * width - 1;
	
	float max = sum[maxId];
	
	//((float*)&sum)[id] = ((float)sum[id] / max);
	((__global float*)sum)[id] = ((float)sum[id] / max);
	//sum[id] = (float)sum[id] / max * 100;
}
