#version 150

//shimmer light
struct Light {
    vec4 color;
    vec3 position;
    float radius;
};

layout (std140) uniform Lights {
    Light lights[2048];     //  16  8 * 2048
};

uniform vec3 CamPos;
uniform int LightCount;

vec4 color_light(vec3 pos, vec4 vertex_color) {
    float sumR = 0;
    float sumG = 0;
    float sumB = 0;
    float count = 0;
    float maxIntens = 0;
    float totalIntens = 0;
    vec3 fragPos = pos + CamPos;
    for (int i = 0; i < LightCount; i++) {
        Light l = lights[i];
        float radius = pow(l.radius, 2);
        vec3 poss = vec3(0.,0.,0.);
        float intensity = pow(max(0., 1. - distance(l.position, fragPos) / l.radius), 2);
        totalIntens += intensity;
        maxIntens = max(maxIntens, intensity);
    }
    for (int i = 0; i < LightCount; i++) {
        Light l = lights[i];
        float radius = pow(l.radius, 2);
        float intensity = pow(max(0, 1.0f - distance(l.position, fragPos) / l.radius), 2);
        sumR += l.color.r * l.color.a * (intensity / totalIntens);
        sumG += l.color.g * l.color.a * (intensity / totalIntens);
        sumB += l.color.b * l.color.a * (intensity / totalIntens);
    }

    vec3 lcolor = vec3(max(sumR * 1.5f, 0.0f), max(sumG * 1.5f, 0.0f), max(sumB * 1.5f, 0.0f));
    float intens = min(1.0f, maxIntens);
    vec3 lcolor_2 = clamp(lcolor.rgb * intens, 0.0f, 1.0f);

    // blend
    return vec4(vertex_color.rgb + lcolor_2, 1.0);
//    return vec4(max(vertex_color.rgb, lcolor_2), 1.0);
}
