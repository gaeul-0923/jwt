package com.example.demo.view.login;



import com.example.demo.api.auth.AuthService;
import com.example.demo.api.auth.LoginVO;
import com.example.demo.common.utill.CommUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;


@Log4j2
@Controller
public class LoginController {

	@Autowired
	private AuthService authService;
	
	@RequestMapping(value = "/login",method = RequestMethod.GET)
	public String login(Model model, HttpServletRequest request) throws Exception{
		LoginVO loginVO = CommUtil.getLoginVO();
		if (loginVO != null && CommUtil.isNotEmpty(loginVO.getUser_id())) {
			return "redirect:/home";
		} else {
			return "login";
		}
	}

	@RequestMapping(value = "/home",method = RequestMethod.GET)
	public String home(Model model, HttpServletRequest request) throws Exception{
		
		/*HttpSession session = (HttpSession)request.getSession();
		if(session != null) {
			List<HashMap<String, Object>> menuList = (List<HashMap<String, Object>>)session.getAttribute("menuList");
			if(menuList != null && menuList.size() > 0) {
				HashMap<String, Object> level1 = (HashMap<String, Object>) menuList.get(0).get("level1");
				
				List<HashMap<String, Object>> level2 = (List<HashMap<String, Object>>)level1.get("level2");
				
				HashMap<String, Object> level2First = (HashMap<String, Object>)level2.get(0);
				
				List<HashMap<String, Object>> level3 = (List<HashMap<String, Object>>)level2First.get("level3");
				
				HashMap<String, Object> level3First = (HashMap<String, Object>)level3.get(0);
				
				log.debug("menu_url=============>>> "+level3First.get("menu_url"));
				
				model.addAttribute("menu_url", level3First.get("menu_url"));
			
			}
		}*/
		
		
		return "home";
	}
}
