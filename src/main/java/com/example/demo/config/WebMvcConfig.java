package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

	//파일 저장경로
	@Value("${spring.servlet.multipart.location}")
	private String location;


	//서버
	@Value("${spring.config.activate.on-profile}")
	private String activateProfile;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

    	//윈도우: file:///D:/DATA/video/  <= /// 세개 추가
    	//리눅스: file:/DATA/video/
    	if("local".equals(activateProfile)) {
    		location = "///" + location;
    	}
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:"+location);

		// 웹뷰(Thymeleaf) static resources
        registry.addResourceHandler("/images/**", "/css/**", "/js/**", "/fonts/**", "/lib/**", "/favicon.ico", "/sample/**")
                .addResourceLocations("classpath:/static/images/", "classpath:/static/css/", "classpath:/static/js/", "classpath:/static/fonts/", "classpath:/static/lib/", "classpath:/static/favicon.ico", "classpath:/static/sample/");

		// API Document static resources
//        registry.addResourceHandler("/docs/**")
//                .addResourceLocations("classpath:/static/docs/");
    }
}
