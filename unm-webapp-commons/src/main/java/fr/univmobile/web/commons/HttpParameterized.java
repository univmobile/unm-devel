package fr.univmobile.web.commons;

import java.lang.reflect.Method;
import java.util.Map;

public interface HttpParameterized {

	Map<Method, String> httpParameterValues();
}