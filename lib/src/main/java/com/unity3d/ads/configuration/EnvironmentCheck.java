package com.unity3d.ads.configuration;

import android.os.Build;

import com.unity3d.ads.log.DeviceLog;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class EnvironmentCheck {
	public static boolean isEnvironmentOk() {
		return testProGuard();
	}

	public static boolean testProGuard() {
		try {
			Class<?> webBridge = Class.forName("com.unity3d.ads.webview.bridge.WebViewBridgeInterface");
			Method handleInvocation = webBridge.getMethod("handleInvocation", String.class);
			Method handleCallback = webBridge.getMethod("handleCallback", String.class, String.class, String.class);

			if(hasJavascriptInterface(handleInvocation) && hasJavascriptInterface(handleCallback)) {
				DeviceLog.debug("Unity Ads ProGuard check OK");
				return true;
			} else {
				DeviceLog.error("Unity Ads ProGuard check fail: missing @JavascriptInterface annotations in Unity Ads web bridge");
				return false;
			}
		} catch(ClassNotFoundException e) {
			DeviceLog.exception("Unity Ads ProGuard check fail: Unity Ads web bridge class not found", e);
			return false;
		} catch(NoSuchMethodException e) {
			DeviceLog.exception("Unity Ads ProGuard check fail: Unity Ads web bridge methods not found", e);
			return false;
		} catch(Exception e) {
			DeviceLog.exception("Unknown exception during Unity Ads ProGuard check: " + e.getMessage(), e);
			// Unknown exception, return test success just to be on the safe side
			return true;
		}
	}

	private static boolean hasJavascriptInterface(Method m) {
		// JavascriptInterface API does not exist when API level is below 17 so skip all checks for old devices
		if (Build.VERSION.SDK_INT < 17) {
			return true;
		}

		Annotation[] annotations = m.getAnnotations();

		if (annotations != null) {
			for (Annotation a : annotations) {
				if (a instanceof android.webkit.JavascriptInterface) {
					return true;
				}
			}
		}

		return false;
	}
}