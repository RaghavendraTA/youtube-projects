package com.temkarstudios.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.temkarstudios.ratelimiter.core.RateLimiter;
import com.temkarstudios.ratelimiter.core.RateLimiterFactory;
import com.temkarstudios.ratelimiter.core.RateLimitersEnum;

@Component
public class RequestMiddleware implements HandlerInterceptor {

    private final RateLimiter rateLimiter;
    
    public RequestMiddleware() {
        // Bucket will reset with 2 tokens every second, allowing 2 requests per second
        this.rateLimiter = RateLimiterFactory.createRateLimiter(RateLimitersEnum.TOKEN_BUCKET, 2, 1000);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
        // Checking if the URL is valid and extracting the API path for rate limiting
        String[] urlParts = request.getRequestURL().toString().split("/");
        if (urlParts.length == 0 || urlParts[urlParts.length - 2].compareTo("api") != 0) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid API Path");
            return false;
        }

        String apiPath = urlParts[urlParts.length - 1]; // Get the last part of the URL as the key for rate limiting
        */
        String clientIp = request.getRemoteAddr();
        
        // Check if rate limit allows this request
        if (!rateLimiter.allowRequests(clientIp)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum requests exceeded. Please try again later.\"}");
            return false;
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // Logic after the controller is executed but before the view is rendered
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // Logic after the complete request has finished (e.g., cleanup tasks)
    }
}
