/**
 *Copyright [2012] [Ghetolay]
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package com.github.ghetolay.jwamp.utils;

import java.util.List;
import java.util.Map;

/**
 * @author ghetolay
 *
 */
public class DynamicValueBuilder {


	public static DynamicValue fromObject(Object obj){
		if(obj == null)
			return null;
		
		if(obj instanceof BooleanValue)
			return new BooleanValue((Boolean)obj);
		
		if(obj instanceof Integer)
			return new DoubleValue((Integer)obj);
			
		if(obj instanceof Float)
			return new DoubleValue((Float)obj);
			
		if(obj instanceof Long)
			return new DoubleValue((Long)obj);
			
		if(obj instanceof Double)
			return new DoubleValue((Double)obj);
		
		if(obj instanceof String)
			return new StringValue((String)obj);
		
		//change it ?
		if(obj instanceof List)
			return new ListValue((List<?>)obj, false);
			
		//change it ?
		if(obj instanceof Map)
			return new MapValue((Map<?, ?>)obj, true);
		
		throw new DynamicValueException("Unknow Value Type " + obj.getClass());
	}

	private static class BooleanValue extends AbstractDynamicValue{
		boolean value;
		
		BooleanValue(boolean value){
			this.value = value;
		}
		
		@Override
		public boolean isBoolean()  {return true;}
		@Override
		public boolean asBoolean()  {return value;}
	
		public Object  asObject()   {return value;}
	}
	
	private static class DoubleValue extends AbstractDynamicValue{
		double value;
		
		DoubleValue(double value){
			this.value = value;
		}
		
		@Override
		public boolean isNumber()   {return true;}
		@Override
		public double  asNumber()  {return value;}
		
		public Object  asObject()   {return value;}
	}
	
	private static class IntegerValue extends AbstractDynamicValue{
		int value;
		
		IntegerValue(int value){
			this.value = value;
		}
		
		@Override
		public boolean isNumber()   {return true;}
		@Override
		public double  asNumber()  {return value;}
		
		public Object  asObject()   {return value;}
	}
	
	private static class StringValue extends AbstractDynamicValue{
		String value;
		
		StringValue(String value){
			this.value = value;
		}
		
		@Override
		public boolean isString()   {return true;}
		@Override
		public String    asString()  {return value;}
		
		public Object  asObject()   {return value;}
	}
	
	private static class ListValue extends AbstractDynamicValue{
		List<?> value;
		boolean object;
		
		ListValue(List<?> value, boolean object){
			this.value = value;
			this.object = object;
		}
		
		@Override
		public boolean isList()   {return true;}
		@Override
		public List<?> asList()  {return value;}
		
		public Object  asObject()   {return value;}
		
		@Override
		public boolean maybeObject() {return object;}
	}
	
	private static class MapValue extends AbstractDynamicValue{
		Map<?,?> value;
		boolean object;
		
		MapValue(Map<?,?> value, boolean object){
			this.value = value;
			this.object = object;
		}
		
		@Override
		public boolean  isMap()   {return true;}
		@Override
		public Map<?,?> asMap()  {return value;}
		
		public Object  asObject()   {return value;}
		
		@Override
		public boolean maybeObject() {return object;}
	}
	
	private static abstract class AbstractDynamicValue implements DynamicValue{

		public boolean isBoolean()   {return false;}
		public boolean isNumber()    {return false;}
		public boolean isString()    {return false;}
		public boolean isList()      {return false;}
		public boolean isMap()       {return false;}
		public boolean maybeObject() {return false;}

		public boolean    asBoolean()  {throw new DynamicValueException();}
		public double     asNumber()   {throw new DynamicValueException();}
		public String     asString()   {throw new DynamicValueException();}
		public List<?>    asList()     {throw new DynamicValueException();}
		public Map<?, ?>  asMap()      {throw new DynamicValueException();}
	}

	private static class DynamicValueException extends RuntimeException{
		private static final long serialVersionUID = -6667860784049423058L;	
		
		public DynamicValueException(){}
		
		public DynamicValueException(String msg){
			super(msg);
		}
	}
	
}
