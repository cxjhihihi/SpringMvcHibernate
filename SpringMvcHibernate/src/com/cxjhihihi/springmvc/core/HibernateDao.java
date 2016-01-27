package com.cxjhihihi.springmvc.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.type.Type;




/**
 * 2.0
 */
public interface HibernateDao<T, ID extends Serializable> {	
	/**
	 * 持久化一个对象(save)
	 */
	public Serializable save(T entity);
	
	/**
	 * 持久化一个对象(update)
	 */
	public void update(T entity);
	
	/**
	 * 持久化一个对象(save or update)
	 */
	public void makePersistent(T entity);

	/**
	 * 产生一个游离对象
	 */
	public void delete(T entity);
	
	/**
	 * 删除持久化对象集合
	 * @param entities
	 */
	public void deleteAll(Collection<T> entities);
	
	/**
	 * 删除持久化对象集合
	 * @param clazz
	 * @param ids
	 */
	public void deleteAll(Class<T> clazz,ID[] ids);
	
	/**
	 * 通过ID获取对应的实体对象
	 * 
	 * @return T
	 */
	public T findById(Class<T> clazz, ID id);
	
	/**
	 * 通过ID数组获取对应的实体对象数组
	 * @param clazz
	 * @param ids
	 * @return
	 */
	public List<T> findAllById(Class<T> clazz, ID[] ids);

	/**
	 * 通过Example方法检索实体对象
	 * 
	 * @return List<T>
	 */
	public List<T> findByExample(T exampleInstance);

	/**
	 * 通过Example方法检索实体对象
	 * 
	 * @return List<T>
	 */
	public List<T> findByExample(T exampleInstance, String[] excludeProperty);

	public List<T> find(String queryString);
	
	/**
	 * 使用HQL语句进行查询，并提供一个可设置参数
	 * @param queryString
	 * @param param
	 * @return
	 */
	public List<T> find(String queryString, Object param);
	
	/**
	 * 使用HQL语句进行查询，并提供一组可设置参数
	 * @param queryString
	 * @param params
	 * @return
	 */
	public List<T> find(String queryString, Object[] params);
	
	/**
	 * 执行SQL
	 * @param sql
	 * @return
	 */
	public Object executeSQL(final String sql);
	
	/**
	 * 执行HQL
	 * @param hql
	 * @return
	 */
	public Object executeHQL(final String hql);
		
	/**
	 * 执行SQL
	 * @param sql
	 * @param paramMap
	 * @return
	 */
	public List executeSqlFind(final String sql,final Map<String, Object> paramMap);
	public List executeHqlFind(final String hql,final Map<String, Object> paramMap) ;
}
