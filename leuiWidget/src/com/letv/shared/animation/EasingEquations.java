package com.letv.shared.animation;

/*
 * refer to  http://gizma.com/easing/
 *           http://isux.tencent.com/animation-factor.html
 *           http://gsgd.co.uk/sandbox/jquery/easing/jquery.easing.1.3.js
 */
public class EasingEquations {
    
    // t - current time
    // b - start value
    // c - change in value
    // d - duration
    // t and d can be frames or seconds/milliseconds

    // simple linear tweening - no easing, no acceleration
    public static float linearTween(float t, float b, float c, float d) {
        return c*t/d + b;
    }
    
    
    // quadratic easing in - accelerating from zero velocity
    public static float easeInQuad(float t, float b, float c, float d) {
        t /= d;
        return c*t*t + b;
    }
    
    // quadratic easing out - decelerating to zero velocity
    public static float easeOutQuad(float t, float b, float c, float d) {
        t /= d;
        return -c * t*(t-2) + b;
    }
    
    // quadratic easing in/out - acceleration until halfway, then deceleration
    public static float easeInOutQuad(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return c/2*t*t + b;
        t--;
        return -c/2 * (t*(t-2) - 1) + b;
    }
    
    // cubic easing in - accelerating from zero velocity
    public static float easeInCubic(float t, float b, float c, float d) {
        t /= d;
        return c*t*t*t + b;
    }
    
    // cubic easing out - decelerating to zero velocity
    public static float easeOutCubic(float t, float b, float c, float d) {
        t /= d;
        t--;
        return c*(t*t*t + 1) + b;
    }
    
    // cubic easing in/out - acceleration until halfway, then deceleration
    public static float easeInOutCubic(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return c/2*t*t*t + b;
        t -= 2;
        return c/2*(t*t*t + 2) + b;
    }
    
    
    // quartic easing in - accelerating from zero velocity
    public static float easeInQuart(float t, float b, float c, float d) {
        t /= d;
        return c*t*t*t*t + b;
    }
    
    // quartic easing out - decelerating to zero velocity
    public static float easeOutQuart(float t, float b, float c, float d) {
        t /= d;
        t--;
        return -c * (t*t*t*t - 1) + b;
    }
    
    // quartic easing in/out - acceleration until halfway, then deceleration
    public static float easeInOutQuart(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return c/2*t*t*t*t + b;
        t -= 2;
        return -c/2 * (t*t*t*t - 2) + b;
    }
    
    
    // quintic easing in - accelerating from zero velocity
    public static float easeInQuint(float t, float b, float c, float d) {
        t /= d;
        return c*t*t*t*t*t + b;
    }

    // quintic easing out - decelerating to zero velocity
    public static float easeOutQuint(float t, float b, float c, float d) {
        t /= d;
        t--;
        return c*(t*t*t*t*t + 1) + b;
    }

    // quintic easing in/out - acceleration until halfway, then deceleration
    public static float easeInOutQuint(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return c/2*t*t*t*t*t + b;
        t -= 2;
        return c/2*(t*t*t*t*t + 2) + b;
    }
    
    
    // sinusoidal easing in - accelerating from zero velocity
    public static float easeInSine(float t, float b, float c, float d) {
        return -c * (float)Math.cos(t/d * (Math.PI/2)) + c + b;
    }

    // sinusoidal easing out - decelerating to zero velocity
    public static float easeOutSine(float t, float b, float c, float d) {
        return c * (float)Math.sin(t/d * (Math.PI/2)) + b;
    }

    
    // sinusoidal easing in/out - accelerating until halfway, then decelerating
    public static float easeInOutSine(float t, float b, float c, float d) {
        return -c/2 * (float)(Math.cos(Math.PI*t/d) - 1) + b;
    }
      
    
    // exponential easing in - accelerating from zero velocity
    public static float easeInExpo(float t, float b, float c, float d) {
        return c * (float)Math.pow( 2, 10 * (t/d - 1) ) + b;
    }

    // exponential easing out - decelerating to zero velocity
    public static float easeOutExpo(float t, float b, float c, float d) {
        return c * (float)( -Math.pow( 2, -10 * t/d ) + 1 ) + b;
    }

    // exponential easing in/out - accelerating until halfway, then decelerating
    public static float easeInOutExpo(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return c/2 * (float)Math.pow( 2, 10 * (t - 1) ) + b;
        t--;
        return c/2 * (float)( -Math.pow( 2, -10 * t) + 2 ) + b;
    }
    
    
    // circular easing in - accelerating from zero velocity
    public static float easeInCirc(float t, float b, float c, float d) {
        t /= d;
        return -c * (float)(Math.sqrt(1 - t*t) - 1) + b;
    }

    // circular easing out - decelerating to zero velocity
    public static float easeOutCirc(float t, float b, float c, float d) {
        t /= d;
        t--;
        return c * (float)Math.sqrt(1 - t*t) + b;
    }
    
    // circular easing in/out - acceleration until halfway, then deceleration
    public static float easeInOutCirc(float t, float b, float c, float d) {
        t /= d/2;
        if (t < 1) return -c/2 * (float)(Math.sqrt(1 - t*t) - 1) + b;
        t -= 2;
        return c/2 * (float)(Math.sqrt(1 - t*t) + 1) + b;
    }
    
}
