package com.example.demo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleResourceNotFound(ResourceNotFoundException e, Model model) {
		model.addAttribute("status", 404);
		model.addAttribute("error", "Not Found");
		model.addAttribute("message", e.getMessage());
		return "error";
	}

	@ExceptionHandler(BusinessLogicException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String handleBusinessLogicError(BusinessLogicException e, Model model) {
		model.addAttribute("status", 400);
		model.addAttribute("error", "Bad Request");
		model.addAttribute("message", e.getMessage());
		return "error";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleGeneralException(Exception e, Model model) {
		e.printStackTrace(); // ログに残す
		model.addAttribute("status", 500);
		model.addAttribute("error", "Internal Server Error");
		model.addAttribute("message", "予期せぬエラーが発生しました。");
		return "error";
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String handleDataIntegrityViolation(DataIntegrityViolationException e, Model model) {

		model.addAttribute("status", 409); // 409 Conflict (競合)
		model.addAttribute("error", "Conflict");
		model.addAttribute("message", "既に登録されているか、無効な操作が行われました。（重複エラー）");

		return "error";
	}

	//TMDB APIなどの外部連携エラー (4xx, 5xx)
	// 引数に HttpServletResponse を追加し、動的にステータスをセットする
	@ExceptionHandler(WebClientResponseException.class)
	public String handleWebClientError(WebClientResponseException e,HttpServletResponse response, Model model) {
		// 開発者用ログ（どんなステータスコードが返ってきたか）
		System.err.println("TMDB API Error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
		
		// 実際のHTTPレスポンスにもステータスコードを適用
        response.setStatus(e.getStatusCode().value());
        
		model.addAttribute("status", e.getStatusCode().value());
		model.addAttribute("error", "External API Error");
		model.addAttribute("message", "映画データの取得中にエラーが発生しました。(TMDB API)");

		return "error";
	}

	// TMDB APIに繋がらない（オフライン、DNSエラーなど）
	@ExceptionHandler(WebClientRequestException.class)
	@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
	public String handleWebClientRequestError(WebClientRequestException e, Model model) {
		model.addAttribute("status", 503); // Service Unavailable
		model.addAttribute("error", "Service Unavailable");
		model.addAttribute("message", "外部システムに接続できませんでした。ネットワーク状況を確認してください。");

		return "error";
	}
}