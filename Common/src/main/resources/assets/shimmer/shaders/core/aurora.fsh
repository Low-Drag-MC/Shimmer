#version 150
//from https://www.shadertoy.com/view/MsjfRG

uniform sampler2D background;

in vec2 uv;

uniform vec4 ColorModulator;
uniform float iTime;
uniform float Ratio;
uniform float intensity;
uniform vec2 screen;

out vec4 fragColor;

// Noise functions
float hash(vec2 co) { return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453); }
float hash(float x, float y) { return hash(vec2(x, y)); }

float shash(vec2 co)
{
    float x = co.x;
    float y = co.y;

    float corners = (hash(x-1., y-1.) + hash(x+1., y-1.) + hash(x-1., y+1.) + hash(x+1., y+1.))/16.;
    float sides   = (hash(x-1., y) + hash(x+1., y) + hash(x, y-1.) + hash(x, y+1.))/8.;
    float center  = hash(co) / 4.;

    return corners + sides + center;
}

float noise(vec2 co)
{
    vec2 pos  = floor(co);
    vec2 fpos = co - pos;

    fpos = (3.0 - 2.0*fpos)*fpos*fpos;

    float c1 = shash(pos);
    float c2 = shash(pos + vec2(0.0, 1.0));
    float c3 = shash(pos + vec2(1.0, 0.0));
    float c4 = shash(pos + vec2(1.0, 1.0));

    float s1 = mix(c1, c3, fpos.x);
    float s2 = mix(c2, c4, fpos.x);

    return mix(s1, s2, fpos.y);
}

// Perlin Noise
float pnoise(vec2 co, int oct)
{
    float total = 0.0;
    float m = 0.0;

    for(int i=0; i<oct; i++)
    {
        float freq = pow(2.0, float(i));
        float amp  = pow(0.5, float(i));

        total += noise(freq * co) * amp;
        m += amp;
    }

    return total/m;
}


// fbm(Fractal Brownian Motion): repeatedly apply Perlin noise to position
vec2 fbm(vec2 p, int oct)
{
    return vec2(pnoise(p + vec2(iTime, 0.0), oct), pnoise(p + vec2(-iTime, 0.0), oct));
}

float fbm2(vec2 p, int oct)
{
    return pnoise(p + 10.*fbm(p, oct) + vec2(0.0, iTime), oct);
}

// Calculate the lights themselves
vec3 lights(vec2 co)
{
    float d,r,g,b,h;
    vec3 rc,gc,bc,hc;

    // Red (top)
    r = fbm2(co * vec2(1.0, 0.5), 10);
    d = pnoise(2.*co+vec2(0.3*iTime), 10);
    rc = vec3(1, 0.0, 0.0) * r * smoothstep(0.0, 2.5+d*r, co.y) * smoothstep(-5., 1., 5.-co.y-2.*d);

    // Green (middle)
    g = fbm2(co * vec2(2., 0.5), 4);
    gc = 0.8*vec3(0.5,1.0,0.0) * clamp(2.*pow((3.-2.*g)*g*g,2.5)-0.5*co.y, 0.0, 1.0) * smoothstep(-2.*d, 0.0, co.y) * smoothstep(0.0, 0.3, 1.1+d-co.y);

    g = fbm2(co * vec2(1.0, 0.2), 2);
    gc += 0.5*vec3(0.5,1.0,0.0) * clamp(2.*pow((3.-2.*g)*g*g,2.5)-0.5*co.y, 0.0, 1.0) * smoothstep(-2.*d, 0.0, co.y) * smoothstep(0.0, 0.3, 1.1+d-co.y);


    // Blue (bottom)
    h = pnoise(vec2(5.0*co.x, 5.0*iTime), 1);
    hc = vec3(0.0, 0.8, 1.0) * pow(h+0.1,2.0) * smoothstep(-2.*d, 0.0, co.y+0.2) * smoothstep(-h, 0.0, -co.y-0.4);

    return rc+gc+hc; //c for component
}

vec4 aurora_color()
{
    vec4 fragColor;
    vec2 co = vec2(uv.x * Ratio,uv.y);

    vec3 col = vec3(0.0);


    // Aurora (with some transformation)
    float s = 0.1*sin(iTime);
    //float f = 0.6+uv.x*(0.4+uv.x*(-1.5-s+uv.x*(1.3+s)));
    float f = 0.0+0.4*pnoise(vec2(5.*uv.x, 0.3*iTime),1);
    vec2 aco = co;
    aco.y -= f;
    aco *= 10.*uv.x+5.0;
    col += 1.3*lights(aco)
    * (smoothstep(0.3, 0.6, pnoise(vec2(10.*uv.x, 0.3*iTime),1))
    +  0.5*smoothstep(0.5, 0.7, pnoise(vec2(10.*uv.x,iTime),1)));

    fragColor = vec4(col,1.0);
    return fragColor;
}

vec3 jodieReinhardTonemap(vec3 c){
    float l = dot(c, vec3(0.2126, 0.7152, 0.0722));
    vec3 tc = c / (c + 1.0);

    return mix(c / (l + 1.0), tc, tc);
}

void main() {
    vec4 auroraColor = aurora_color();
    vec4 backgroundColor = texture(background,gl_FragCoord.xy/screen);
    vec2 dis = min(abs(uv),abs(uv-1));
    float x = min(dis.x,dis.y);
    float y = min(smoothstep(0,0.2,x),1.);
    fragColor = vec4(jodieReinhardTonemap((auroraColor * y * intensity).xyz)+backgroundColor.xyz,length(auroraColor) * y);
}
