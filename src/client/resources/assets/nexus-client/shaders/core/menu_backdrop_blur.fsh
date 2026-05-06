#version 330

#moj_import <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float roundedMask(vec2 localUv, vec2 radius) {
    vec2 clampedRadius = max(radius, vec2(0.0001));
    vec2 edgeDistance = min(localUv, 1.0 - localUv);

    if (edgeDistance.x >= clampedRadius.x || edgeDistance.y >= clampedRadius.y) {
        return 1.0;
    }

    vec2 cornerDelta = (edgeDistance - clampedRadius) / clampedRadius;
    float ellipseDistance = dot(cornerDelta, cornerDelta);
    return 1.0 - smoothstep(1.0, 1.08, ellipseDistance);
}

void main() {
    vec2 localUv = vertexColor.rg;
    vec2 radius = vertexColor.ba;
    float mask = roundedMask(localUv, radius);
    if (mask <= 0.001) {
        discard;
    }

    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec2 offset1 = texel * 1.3846153846;
    vec2 offset2 = texel * 3.2307692308;

    vec4 blurred = texture(Sampler0, texCoord0) * 0.20;
    blurred += texture(Sampler0, texCoord0 + vec2(offset1.x, 0.0)) * 0.10;
    blurred += texture(Sampler0, texCoord0 - vec2(offset1.x, 0.0)) * 0.10;
    blurred += texture(Sampler0, texCoord0 + vec2(offset2.x, 0.0)) * 0.06;
    blurred += texture(Sampler0, texCoord0 - vec2(offset2.x, 0.0)) * 0.06;
    blurred += texture(Sampler0, texCoord0 + vec2(0.0, offset1.y)) * 0.10;
    blurred += texture(Sampler0, texCoord0 - vec2(0.0, offset1.y)) * 0.10;
    blurred += texture(Sampler0, texCoord0 + vec2(0.0, offset2.y)) * 0.06;
    blurred += texture(Sampler0, texCoord0 - vec2(0.0, offset2.y)) * 0.06;
    blurred += texture(Sampler0, texCoord0 + offset1) * 0.04;
    blurred += texture(Sampler0, texCoord0 - offset1) * 0.04;
    blurred += texture(Sampler0, texCoord0 + vec2(offset1.x, -offset1.y)) * 0.04;
    blurred += texture(Sampler0, texCoord0 + vec2(-offset1.x, offset1.y)) * 0.04;
    fragColor = vec4(blurred.rgb, blurred.a * mask) * ColorModulator;
}
