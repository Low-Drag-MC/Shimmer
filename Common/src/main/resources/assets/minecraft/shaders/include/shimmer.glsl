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

layout (std140) uniform Env {
    int lightCount;
    vec3 camPos;
};

vec4 color_light(vec3 pos, vec4 vertex_color) {
    vec3 lightColor = vec3(0., 0., 0.);
    vec3 fragPos = pos + camPos;
    for (int i = 0; i < lightCount; i++) {
        Light l = lights[i];
        float intensity = smoothstep(0., 1., 1. - distance(l.position, fragPos) / l.radius);
        lightColor += l.color.rgb * l.color.a * intensity;
    }

    vec3 lcolor_2 = clamp(lightColor.rgb, 0.0f, 1.0f);

    // blend
    return vec4(vertex_color.rgb + lcolor_2, 1.0);
}

vec4 color_light_uv(vec3 pos, vec4 vertex_color,ivec2 uv) {
    float blockLight = smoothstep(0.5 / 16.0, 20.5 / 16.0, uv.x / 256.0);

    if (blockLight > 0. && uv.x < 255.) {
        vec3 lightColor = vec3(0., 0., 0.);
        vec3 fragPos = pos + camPos;
        for (int i = 0; i < lightCount; i++) {
            Light l = lights[i];
            float intensity = smoothstep(0., 1., 1. - distance(l.position, fragPos) / l.radius);
            lightColor += l.color.rgb * l.color.a * intensity;
        }

        return vec4(vertex_color.rgb + clamp(lightColor.rgb * blockLight * 3.5, 0.0, 1.0), 1.0);
    } else {
        return vertex_color;
    }
}

// for rubidium
vec4 rb_color_light_uv(vec3 pos, vec4 vertex_color, vec2 uv) {
    float blockLight = uv.x;

    if (blockLight > 0. && blockLight <= 0.97) {
        vec3 lightColor = vec3(0., 0., 0.);
        vec3 fragPos = pos + camPos;
        for (int i = 0; i < lightCount; i++) {
            Light l = lights[i];
            float intensity = smoothstep(0., 1., 1. - distance(l.position, fragPos) / l.radius);
            lightColor += l.color.rgb * l.color.a * intensity;
        }

        return vec4(vertex_color.rgb + clamp(lightColor.rgb * blockLight * 3.5, 0.0, 1.0), 1.0);
    } else {
        return vertex_color;
    }
}
