#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
out vec4 fragColor;


vec3 ApplyBloomThreshold (vec3 color) {
    vec4 _BloomThreshold = vec4(1.);
    float t = 1;
    float k = 0.5;
    _BloomThreshold.x = pow((t + 0.055) / 1.055, 2.4);
    _BloomThreshold.y = _BloomThreshold.x * k;
    _BloomThreshold.z = 2. * _BloomThreshold.y;
    _BloomThreshold.w = 0.25 / (_BloomThreshold.y + 0.00001);
    _BloomThreshold.y = -_BloomThreshold.x;

    float brightness = max(max(color.r, color.g), color.b);
    float soft = brightness + _BloomThreshold.y;
    soft = clamp(soft, 0.0, _BloomThreshold.z);
    soft = soft * soft * _BloomThreshold.w;
    float contribution = max(soft, brightness - _BloomThreshold.x);
    contribution /= max(brightness, 0.00001);
    return color * contribution;
}

void main(){
    fragColor = vec4(ApplyBloomThreshold(texture(DiffuseSampler, texCoord).rgb), 1.0);
}
