package com.example.demo.common.response;

import lombok.Getter;
import lombok.Setter;


/**
 * medic_doctor
 * 파일
 * @author yuritrap
 * @since 2022.02.18
 */
@Getter
@Setter
public class UploadFileVO {
	private String fieldName; //request name값
	private String oriFileName; //원본 파일명
	private String fileName;    //실제 업로드 파일명
	private String ext;
	private String serverFullPath; // serverlocation +file.separator+ serverpath  /data/ddd/20120101  (마지막 File.separator 없음)
	private String serverPath;     //서버 위치경로 location 뺀 위치만  /ddd/20120101  (마지막 File.separator 없음)
	private String serverlocation; //application.yml 의 multipart location 값 c:\\data  (마지막 File.separator 없음)
	private String contentType;
	private String mimeType;       //file upload 한 mimetype
	private long size;
	
	
	

}
