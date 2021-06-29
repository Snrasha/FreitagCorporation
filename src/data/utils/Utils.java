
package src.data.utils;

import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.dark.shaders.distortion.WaveDistortion;
import org.lwjgl.util.vector.Vector2f;


public class Utils {
    
    public static void CustomRippleDistortion(Vector2f loc, Vector2f vel, float size, float intensity, boolean flip, float angle, float arc, float edgeSmooth, float fadeIn, float last, float fadeOut, float growthTime, float shrinkTime) {

        RippleDistortion ripple = new RippleDistortion(loc, vel);

        ripple.setIntensity(intensity);
        ripple.setSize(size);
        ripple.setArc(angle - arc / 2, angle + arc / 2);
        if (edgeSmooth != 0) {
            ripple.setArcAttenuationWidth(edgeSmooth);
        }
        ripple.flip(flip);
        if (fadeIn != 0) {
            ripple.fadeInIntensity(fadeIn);
        }
        ripple.setLifetime(last);
        if (fadeOut != 0) {
            ripple.setAutoFadeIntensityTime(fadeOut);
        }
        if (growthTime != 0) {
            ripple.fadeInSize(growthTime);
        }
        if (shrinkTime != 0) {
            ripple.setAutoFadeSizeTime(shrinkTime);
        }
        ripple.setFrameRate(60);
        DistortionShader.addDistortion(ripple);

    }
    public static void CustomBubbleDistortion (Vector2f loc, Vector2f vel, float size, float intensity, boolean flip, float angle, float arc, float edgeSmooth, float fadeIn, float last, float fadeOut, float growthTime, float shrinkTime){
                
        WaveDistortion wave = new WaveDistortion(loc, vel);

        wave.setIntensity(intensity);
        wave.setSize(size);
        wave.setArc(angle-arc/2,angle+arc/2);
        if(edgeSmooth!=0){
            wave.setArcAttenuationWidth(edgeSmooth);            
        }
        wave.flip(flip);
        if(fadeIn!=0){
            wave.fadeInIntensity(fadeIn);
        }
        wave.setLifetime(last);
        if(fadeOut!=0){
            wave.setAutoFadeIntensityTime(fadeOut);
        } else {
            wave.setAutoFadeIntensityTime(99);
        }
        if(growthTime!=0){
            wave.fadeInSize(growthTime);
        }
        if(shrinkTime!=0){
            wave.setAutoFadeSizeTime(shrinkTime);
        } else {
            wave.setAutoFadeSizeTime(99);
        }
        DistortionShader.addDistortion(wave);
    }
}
