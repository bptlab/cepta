package org.bptlab.cepta.osiris;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.HtmlUtils;

@Controller
public class WebsocketController {

  @RequestMapping(value = "/api/trainid", method = RequestMethod.POST)
  public ResponseEntity update(@RequestBody String id) {
    return new ResponseEntity(HttpStatus.OK);
  }

/*
  @MessageMapping("/id")
	@SendTo("/topic/traindata")
	public String traindata(String id) throws Exception {
		return id;
	}
*/

}



