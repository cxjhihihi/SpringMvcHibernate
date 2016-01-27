/**
 * 
 */
package com.cxjhihihi.springmvc.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cxjhihihi.springmvc.core.HibernateDao;
import com.cxjhihihi.springmvc.domain.User;

/** 
 *
 *@author hzcaixinjia
 * 
 *@date 2016-1-27 
 * 
 */
@Transactional
@Service("userService")
public class UserServiceImpl implements UserService{

	@Autowired
	@Qualifier("hibernateDao")
	HibernateDao dao;
	public void addUser(User user) {
		// TODO Auto-generated method stub
		dao.save(user);
	}

}
