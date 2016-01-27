package com.cxjhihihi.springmvc.core;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
/** 
 * 
 *@ClassName: HibernateDaoImp 
 *
 *@描述:
 *
 *@author hzcaixinjia
 * 
 *@date 2015年4月29日 
 * 
 */ 
@SuppressWarnings("unchecked")
public class HibernateDaoImp<T, ID extends Serializable> implements HibernateDao<T, ID> {

	protected HibernateTemplate hibernateTemplate;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.hibernateTemplate = new HibernateTemplate(sessionFactory);
	}

	public Serializable save(T entity) {
		return hibernateTemplate.save(entity);
	}
	
	public void update(T entity) {
		hibernateTemplate.update(entity);
	}
	
	public void makePersistent(T entity) {
		hibernateTemplate.saveOrUpdate(entity);
	}

	public void delete(T entity) {
		hibernateTemplate.delete(entity);
	}
	
	public void deleteAll(Collection<T> entities)
	{
		hibernateTemplate.deleteAll(entities);
	}
	
	public void deleteAll(Class<T> clazz,ID[] ids)
	{
		deleteAll(findAllById(clazz, ids));
	}

	public T findById(Class<T> clazz, ID id) {
		return (T) hibernateTemplate.get(clazz, id);
	}
	
	public List<T> findAllById(Class<T> clazz, ID[] ids) {
		List<T> list=new LinkedList<T>();
		for(int i=0;i<ids.length;i++)
		{
			list.add(findById(clazz, ids[i]));
		}
		return list;
	}

	public List<T> findByExample(T exampleInstance) {
		DetachedCriteria detachedCrit = DetachedCriteria
				.forClass(exampleInstance.getClass());
		Example example = Example.create(exampleInstance);
		detachedCrit.add(example);
		return hibernateTemplate.findByCriteria(detachedCrit);
	}

	public List<T> findByExample(T exampleInstance, String[] excludeProperty) {
		DetachedCriteria detachedCrit = DetachedCriteria
				.forClass(exampleInstance.getClass());
		Example example = Example.create(exampleInstance);
		for (String exclude : excludeProperty) {
			example.excludeProperty(exclude);
		}
		detachedCrit.add(example);
		return hibernateTemplate.findByCriteria(detachedCrit);
	}
	
	/**
	 * 取得查询的记录总数(分页显示用)
	 * 
	 * @param session Hibernate的Session对象
	 * @param queryString 查询语句，可以是HQL或者SQL
	 * @param isSQLQuery queryString是否为HQL
	 * @param params 参数
	 * @return int 查询的记录总数
	 */
	private int getTotalCount(Session session, String queryString, Object params[], boolean isHQL) {
		int sqlFrom = queryString.indexOf("from");
		int sqlGroupby = queryString.indexOf("group");
		int sqlOrderby = queryString.indexOf("order by");
		String countStr = "";
		// 因为此方法只取得查询的结果总数，所以将查询语句中可能存在的排序语句去掉来提高查询效率
		if (sqlGroupby > 0) {
			if(sqlOrderby > 0){
				countStr = "select count(0) from (select count(*) "+ queryString.substring(sqlFrom, sqlOrderby)+") a";
			}else{
				countStr = "select count(0) from (select count(*) "+ queryString.substring(sqlFrom)+") a";
			}
		} else if (sqlOrderby > 0) {
			countStr = "select count(*) " + queryString.substring(sqlFrom, sqlOrderby);
		} else {
			countStr = "select count(*) " + queryString.substring(sqlFrom);
		}
		if(isHQL) { //HQL
			Long amount = new Long(0);
			Query query = session.createQuery(countStr);
			if(params!=null&&params.length>0){
				for(int i=0;i!=params.length;i++)query.setParameter(i, params[i]);
			}
			if (!query.list().isEmpty()) {
				amount = (Long) query.list().get(0);
			} else {
				return 0;
			}
			return amount.intValue();			
		} else { //SQL
			BigInteger amount = BigInteger.valueOf(0);
			Query query = session.createSQLQuery(countStr);
			if(params!=null&&params.length>0){
				for(int i=0;i!=params.length;i++)query.setParameter(i, params[i]);
			}
			if (!query.list().isEmpty()) {
				amount = (BigInteger)query.list().get(0);
			} else {
				return 0;
			}
			return amount.intValue();
		}

	}
	/**
	 * 取得查询的记录总数(分页显示用)
	 * 
	 * @param session Hibernate的Session对象
	 * @param queryString 查询语句
	 * @return int 查询的记录总数
	 */
	private int getTotalCountSQL(Session session, String queryString) {
		queryString = queryString.toLowerCase();
		queryString = queryString.replaceAll("\\s+", " ");
		int sqlFrom = queryString.indexOf("from");
		int sqlGroupby = queryString.lastIndexOf("group");
		int sqlOrderby = queryString.lastIndexOf("order by");
		int index_last_r = 0;
		int count = 0;
		Pattern p = Pattern.compile("[()]");
		Matcher m = p.matcher(queryString);
		while (m.find()) {
			if (queryString.substring(m.start(), m.end()).equals("(")) {
				if (count == 0) {
					String str = queryString.substring(index_last_r, m.start());
					int index_from = str.indexOf("from");
					if (index_from != -1) {
						sqlFrom = index_last_r + index_from;
						break;
					}
				}
				count++;
			} else if (queryString.substring(m.start(), m.end()).equals(")")) {
				count--;
				index_last_r = m.end();
			}
		}
		p = Pattern.compile("[)]");
		m = p.matcher(queryString);
		String countStr = "";
		// 因为此方法只取得查询的结果总数，所以将查询语句中可能存在的排序语句去掉来提高查询效率
		if (sqlGroupby > 0 && !m.find(sqlGroupby)) {
			countStr = "select count(*) from( " + queryString.substring(0, sqlOrderby) + ") t_count";
		} else if (sqlOrderby > 0) {
			countStr = "select count(*) " + queryString.substring(sqlFrom, sqlOrderby);
		} else {
			countStr = "select count(*) " + queryString.substring(sqlFrom);
		}
		BigInteger amount = BigInteger.valueOf(0);
		Query query = session.createSQLQuery(countStr);
		if (!query.list().isEmpty()) {
			amount = (BigInteger)query.list().get(0);
		} else {
			return 0;
		}
		return amount.intValue();

	}
	
	private int getTotalCount(Session session, String queryString, Map<String,Object> paramMap, boolean isHQL) {
		int sqlFrom = queryString.indexOf("from");
		int sqlGroupby = queryString.indexOf("group");
		int sqlOrderby = queryString.indexOf("order by");
		String countStr = "";
		// 因为此方法只取得查询的结果总数，所以将查询语句中可能存在的排序语句去掉来提高查询效率
		if (sqlGroupby > 0) {
			if(sqlOrderby > 0){
				countStr = "select count(0) from (select count(*) "+ queryString.substring(sqlFrom, sqlOrderby)+") a";
			}else{
				countStr = "select count(0) from (select count(*) "+ queryString.substring(sqlFrom)+") a";
			}
		} else if (sqlOrderby > 0) {
			countStr = "select count(*) " + queryString.substring(sqlFrom, sqlOrderby);
		} else {
			countStr = "select count(*) " + queryString.substring(sqlFrom);
		}
		if(isHQL) { //HQL
			Long amount = new Long(0);
			Query query = session.createQuery(countStr);
			if(paramMap!=null) {
				for(Map.Entry entry:paramMap.entrySet()) {
					query.setParameter((String)entry.getKey(), entry.getValue());
				}
			}
			
			if (!query.list().isEmpty()) {
				amount = (Long) query.list().get(0);
			} else {
				return 0;
			}
			return amount.intValue();			
		} else { //SQL
			BigInteger amount = BigInteger.valueOf(0);
			Query query = session.createSQLQuery(countStr);
			if(paramMap!=null) {
				for(Map.Entry entry:paramMap.entrySet()) {
					query.setParameter((String)entry.getKey(), entry.getValue());
				}
			}
			if (!query.list().isEmpty()) {
				amount = (BigInteger)query.list().get(0);
			} else {
				return 0;
			}
			return amount.intValue();
		}

	}
	public List<T> find(String queryString) {
		return hibernateTemplate.find(queryString);
	}

	public List<T> find(String queryString, Object param) {
		return hibernateTemplate.find(queryString, param);
	}

	public List<T> find(String queryString, Object[] params) {
		return hibernateTemplate.find(queryString, params);
	}
		
	public Object executeSQL(final String sql) {
		return hibernateTemplate.execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery(sql);
				return query.executeUpdate();
			}
		});		
	}
	
	public Object executeHQL(final String hql) {
		return hibernateTemplate.execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
				return query.executeUpdate();
			}
		});
	}
	
	public List executeSqlFind(final String sql,final Map<String, Object> paramMap){
		return this.hibernateTemplate.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createSQLQuery(sql);
				if(paramMap!=null) {
					for(Map.Entry entry:paramMap.entrySet()) {
						if( entry.getValue() instanceof List ){
							query.setParameterList((String)entry.getKey(), (List)entry.getValue());
						}else{
							query.setParameter((String)entry.getKey(), entry.getValue());
						}						
					}
				}
				return query.list();
			}
		});
	}
	
	public List executeHqlFind(final String hql,final Map<String, Object> paramMap) {
		return this.hibernateTemplate.executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
				if(paramMap!=null) {
					for(Map.Entry entry:paramMap.entrySet()) {
						if( entry.getValue() instanceof List ){
							query.setParameterList((String)entry.getKey(), (List)entry.getValue());
						}else{
							query.setParameter((String)entry.getKey(), entry.getValue());
						}						
					}
				}
				return query.list();
			}
		});
	}


}
