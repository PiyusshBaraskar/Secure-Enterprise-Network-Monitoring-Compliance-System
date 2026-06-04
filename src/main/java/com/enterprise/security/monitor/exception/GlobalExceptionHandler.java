package com.enterprise.security.monitor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("status", HttpStatus.NOT_FOUND.value());
        mav.addObject("error", "Resource Not Found");
        mav.addObject("message", ex.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("status", HttpStatus.BAD_REQUEST.value());
        mav.addObject("error", "Bad Request / Validation Error");
        mav.addObject("message", ex.getMessage());
        mav.setViewName("error");
        return mav;
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleDefault(Exception ex, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", request.getRequestURL());
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", "Internal Server Error");
        mav.addObject("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred.");
        mav.setViewName("error");
        return mav;
    }
}
