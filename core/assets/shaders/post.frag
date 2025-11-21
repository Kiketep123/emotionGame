#ifdef GL_ES
precision mediump float;
#endif
varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform vec4 u_tint; // rgba
void main(){
  vec4 c = texture2D(u_texture, v_texCoords);
  float d = distance(v_texCoords, vec2(0.5));
  float vig = smoothstep(0.8, 0.2, d);
  gl_FragColor = vec4(mix(c.rgb, c.rgb*(1.0-u_tint.rgb), 0.2)*vig, c.a);
}
