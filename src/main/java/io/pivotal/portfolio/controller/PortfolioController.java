package io.pivotal.portfolio.controller;

import io.pivotal.portfolio.domain.Order;
import io.pivotal.portfolio.domain.Portfolio;
import io.pivotal.portfolio.service.PortfolioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
/**
 * Provides the REST API for the portfolio service.
 * 
 * Provides the following endpoints:
 * <p><ul>
 * <li>GET <code>/portfolio/{id}</code> retrieves the portfolio with given user id.
 * <li>POST <code>/portfolio{id}</code> adds an order to the portfolio with the given user id.
 * </ul><p>
 * 
 * @author David Ferreira Pinto
 *
 */
@RestController
public class PortfolioController {
	private static final Logger logger = LoggerFactory
			.getLogger(PortfolioController.class);

	/**
	 * the service to delegate to.
	 */
	@Autowired
	private PortfolioService service;

	/**
	 * Retrieves the portfolio for the given account.
	 * @return The portfolio with HTTP OK.
	 */
	@PreAuthorize("hasAuthority('ROLE_PORTFOLIO')")
	@RequestMapping(value = "/portfolio", method = RequestMethod.GET)
	public ResponseEntity<Portfolio> getPortfolio() {
		logger.debug("PortfolioController: Retrieving portfolio with user id:" );
		Portfolio folio = service.getPortfolio();
		logger.debug("PortfolioController: Retrieved portfolio:" + folio);
		return new ResponseEntity<Portfolio>(folio, getNoCacheHeaders(), HttpStatus.OK);
	}
	
	private HttpHeaders getNoCacheHeaders() {
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("Cache-Control", "no-cache");
		return responseHeaders;
	}
	/**
	 * Adds an order to the portfolio of the given user.
	 *
	 * @param order The order to add.
	 * @return The order with HTTP CREATED or BAD REQUEST if it couldn't save.
	 */
	@PreAuthorize("hasAuthority('ROLE_TRADE')")
	@RequestMapping(value = "/portfolio", method = RequestMethod.POST)
	public ResponseEntity<Order> addOrder(@RequestBody final Order order,
										  @AuthenticationPrincipal JwtAuthenticationToken token) {
		logger.debug("Adding Order: " + order);
		
		//TODO: can do a test to ensure userId == order.getUserId();
		order.setUserId(token.getName());
		Order savedOrder = service.addOrder(order, token.getToken().getTokenValue());

		logger.debug("Order added: " + savedOrder);
		if (savedOrder != null && savedOrder.getOrderId() != null) {
			return new ResponseEntity<Order>(savedOrder, getNoCacheHeaders(), HttpStatus.CREATED);
		} else {
			logger.warn("Order not saved: " + order);
			return new ResponseEntity<Order>(savedOrder, getNoCacheHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
