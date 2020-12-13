package com.github.ness.packets;

import java.util.UUID;

import com.github.ness.reflect.FieldInvoker;
import com.github.ness.reflect.MethodInvoker;

/**
 * An individual sent packet which is associated with a player, and can be cancelled. <br>
 * <br>
 * Instances of this interface should not be persisted in collections.
 * 
 * @author A248
 *
 */
public interface Packet {

	/**
	 * Cancels this packet. Will prevent it from being sent
	 * 
	 */
	void cancel();

	/**
	 * Gets the UUID of the player associated with this packet
	 * 
	 * @return the uuid of the associated player
	 */
	UUID getAssociatedUniqueId();

	/**
	 * Gets the raw packet object
	 * 
	 * @return the NMS packet
	 */
	Object getRawPacket();

	/*
	 * Reflection helpers
	 */

	// Fields

	/**
	 * Gets a field invoker for a field in this packet
	 * 
	 * @param <T> the field type
	 * @param fieldType the field type class
	 * @param fieldName the field name
	 * @return the field invoker
	 */
	<T> FieldInvoker<T> getField(Class<T> fieldType, String fieldName);

	/**
	 * Gets a field invoker for a field in this packet
	 * 
	 * @param fieldName the field name
	 * @return the field invoker
	 */
	FieldInvoker<?> getField(String fieldName);

	/**
	 * Reflectively gets the value of a field in this packet
	 * 
	 * @param <T> the field type
	 * @param fieldType the field type class
	 * @param fieldName the field name
	 * @return the field invoker
	 */
	<T> T invokeField(Class<T> fieldType, String fieldName);

	/**
	 * Reflectively gets the value of a field in this packet
	 * 
	 * @param fieldName the field name
	 * @return the field invoker
	 */
	Object invokeField(String fieldName);

	// Methods

	/**
	 * Gets a method invoker for a method on this packet which has no parameters
	 * 
	 * @param <R> the return type
	 * @param returnType the return type of the method
	 * @param methodName the method name
	 * @return the method invoker
	 */
	<R> MethodInvoker<R> getMethod(Class<R> returnType, String methodName);

	/**
	 * Gets a method invoker for a method on this packet with the given parameter types
	 * 
	 * @param <R> the return type
	 * @param returnType the return type of the method
	 * @param methodName the method name
	 * @param parameterTypes the parameter types
	 * @return the method invoker
	 */
	<R> MethodInvoker<R> getMethod(Class<R> returnType, String methodName, Class<?>...parameterTypes);

	/**
	 * Gets a method invoker for a method on this packet which has no parameters
	 * 
	 * @param methodName the method name
	 * @return the method invoker
	 */
	MethodInvoker<?> getMethod(String methodName);

	/**
	 * Gets a method invoker for a method on this packet with the given parameter types
	 * 
	 * @param methodName the method name
	 * @param parameterTypes the parameter types
	 * @return the method invoker
	 */
	MethodInvoker<?> getMethod(String methodName, Class<?>...parameterTypes);

	/**
	 * Reflectively calls a method on this packet, with no arguments
	 * 
	 * @param <R> the return type
	 * @param returnType the return type class
	 * @param methodName the method name
	 * @return the return value
	 */
	<R> R invokeMethod(Class<R> returnType, String methodName);

	/**
	 * Reflectively calls a method on this packet, with the given arguments
	 * 
	 * @param <R> the return type
	 * @param returnType the return type class
	 * @param methodName the method name
	 * @param parameterTypes the method parameter types
	 * @param arguments the arguments
	 * @return the return value
	 */
	<R> R invokeMethod(Class<R> returnType, String methodName, Class<?>[] parameterTypes, Object...arguments);

}