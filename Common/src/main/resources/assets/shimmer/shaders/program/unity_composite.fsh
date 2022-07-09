#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DownTexture;
uniform sampler2D Background;
uniform vec2 OutSize;
uniform float BloomIntensive;
uniform float BloomBase;
uniform float BloomThresholdUp;
uniform float BloomThresholdDown;

in vec2 texCoord;
out vec4 fragColor;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture(DiffuseSampler, uv + textel.xx) //1 1
    + texture(DiffuseSampler, uv + textel.xy) // 1 -1
    + texture(DiffuseSampler, uv + textel.yx) // -1 1
    + texture(DiffuseSampler, uv + textel.yy)) * 0.25; // -1 -1
}

vec4 up_sampling(vec3 textel, vec2 uv) {
    return vec4(four_k(textel, uv).rgb + texture(DownTexture, uv).rgb, 1.);
}

vec3 aces(vec3 x) {
    const float a = 2.51;
    const float b = 0.03;
    const float c = 2.43;
    const float d = 0.59;
    const float e = 0.14;
    return clamp((x * (a * x + b)) / (x * (c * x + d) + e), 0.0, 1.0);
}

vec3 aces_tonemap(vec3 color){
    mat3 m1 = mat3(
    0.59719, 0.07600, 0.02840,
    0.35458, 0.90834, 0.13383,
    0.04823, 0.01566, 0.83777
    );
    mat3 m2 = mat3(
    1.60475, -0.10208, -0.00327,
    -0.53108,  1.10813, -0.07276,
    -0.07367, -0.00605,  1.07602
    );
    vec3 v = m1 * color;
    vec3 a = v * (v + 0.0245786) - 0.000090537;
    vec3 b = v * (0.983729 * v + 0.4329510) + 0.238081;
    return pow(clamp(m2 * (a / b), 0.0, 1.0), vec3(1.0 / 2.2));
}

vec3 jodieReinhardTonemap(vec3 c){
    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    vec3 tc = c / (c + 1.0);

    return mix(c / (l + 1.0), tc, tc);
}


void main(){
    vec3 textel = vec3(1., -1., 0.) / OutSize.xyx;
    //    out_colour = up_sampling(textel, texCoord);

    vec4 out_colour = texture(DiffuseSampler, texCoord + textel.xx);
    out_colour += texture(DiffuseSampler, texCoord + textel.xz) * 2.0;
    out_colour += texture(DiffuseSampler, texCoord + textel.xy);
    out_colour += texture(DiffuseSampler, texCoord + textel.yz) * 2.0;
    out_colour += texture(DiffuseSampler, texCoord) * 4.0;
    out_colour += texture(DiffuseSampler, texCoord + textel.zx) * 2.0;
    out_colour += texture(DiffuseSampler, texCoord + textel.yy);
    out_colour += texture(DiffuseSampler, texCoord + textel.zy) * 2.0;
    out_colour += texture(DiffuseSampler, texCoord + textel.yx);

    vec4 highLight = texture(DownTexture, texCoord);
    vec4 bloom = BloomIntensive * vec4(out_colour.rgb * 0.8 / 16. + highLight.rgb * 0.8, 1.);

    vec4 background = texture(Background, texCoord);
    background.rgb = background.rgb * (1 - highLight.a) + highLight.a * highLight.rgb;
    float max = max(background.b, max(background.r, background.g));
    float min = min(background.b, min(background.r, background.g));
    fragColor = vec4(background.rgb + aces(bloom.rgb * ((1. - (max + min) / 2.) * (BloomThresholdUp - BloomThresholdDown) + BloomThresholdDown + BloomBase)), 1.);

//    fragColor = vec4(aces(texture(Background, texCoord).rgb + texture(DiffuseSampler, texCoord).rgb * 1.4), 1.);

//    vec3 color = pow(texture(Background, texCoord).rgb * 24., vec3(2.2));
//    color = pow(color, vec3(2.2));
//    color += pow(texture(DiffuseSampler, texCoord).rgb, vec3(2.2));
//    color = pow(color, vec3(1.0 / 2.2));
//
//    color = jodieReinhardTonemap(color);
//
//    fragColor = vec4(color,1.0);
}