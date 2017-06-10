package io.netty.util;

import io.netty.util.concurrent.EventExecutorGroup;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public interface Pipeline<H, C> {
    /**
     * Inserts a handler at the first position of this pipeline.
     *
     * @param name     the name of the handler to insert first
     * @param handler  the handler to insert first
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    Pipeline<H, C> addFirst(String name, H handler);

    /**
     * Inserts a handler at the first position of this pipeline.
     *
     * @param group    the {@link EventExecutorGroup} which will be used to execute the handler
     *                 methods
     * @param name     the name of the handler to insert first
     * @param handler  the handler to insert first
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    Pipeline<H, C> addFirst(EventExecutorGroup group, String name, H handler);

    /**
     * Appends a handler at the last position of this pipeline.
     *
     * @param name     the name of the handler to append
     * @param handler  the handler to append
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    Pipeline<H, C> addLast(String name, H handler);

    /**
     * Appends a handler at the last position of this pipeline.
     *
     * @param group    the {@link EventExecutorGroup} which will be used to execute the handler
     *                 methods
     * @param name     the name of the handler to append
     * @param handler  the handler to append
     *
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    Pipeline<H, C> addLast(EventExecutorGroup group, String name, H handler);

    /**
     * Inserts a handler before an existing handler of this
     * pipeline.
     *
     * @param baseName  the name of the existing handler
     * @param name      the name of the handler to insert before
     * @param handler   the handler to insert before
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     */
    Pipeline<H, C> addBefore(String baseName, String name, H handler);

    /**
     * Inserts a handler before an existing handler of this
     * pipeline.
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the handler
     *                  methods
     * @param baseName  the name of the existing handler
     * @param name      the name of the handler to insert before
     * @param handler   the handler to insert before
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     */
    Pipeline<H, C> addBefore(EventExecutorGroup group, String baseName, String name, H handler);

    /**
     * Inserts a handler after an existing handler of this
     * pipeline.
     *
     * @param baseName  the name of the existing handler
     * @param name      the name of the handler to insert after
     * @param handler   the handler to insert after
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     */
    Pipeline<H, C> addAfter(String baseName, String name, H handler);

    /**
     * Inserts a handler after an existing handler of this
     * pipeline.
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the handler
     *                  methods
     * @param baseName  the name of the existing handler
     * @param name      the name of the handler to insert after
     * @param handler   the handler to insert after
     *
     * @throws NoSuchElementException
     *         if there's no such entry with the specified {@code baseName}
     * @throws IllegalArgumentException
     *         if there's an entry with the same name already in the pipeline
     * @throws NullPointerException
     *         if the specified baseName or handler is {@code null}
     */
    Pipeline<H, C> addAfter(EventExecutorGroup group, String baseName, String name, H handler);

    /**
     * Inserts handlers at the first position of this pipeline.
     *
     * @param handlers  the handlers to insert first
     *
     */
    Pipeline<H, C> addFirst(H... handlers);

    /**
     * Inserts handlers at the first position of this pipeline.
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the handlers
     *                  methods.
     * @param handlers  the handlers to insert first
     *
     */
    Pipeline<H, C> addFirst(EventExecutorGroup group, H... handlers);

    /**
     * Inserts handlers at the last position of this pipeline.
     *
     * @param handlers  the handlers to insert last
     *
     */
    Pipeline<H, C> addLast(H... handlers);

    /**
     * Inserts handlers at the last position of this pipeline.
     *
     * @param group     the {@link EventExecutorGroup} which will be used to execute the handlers
     *                  methods.
     * @param handlers  the handlers to insert last
     *
     */
    Pipeline<H, C> addLast(EventExecutorGroup group, H... handlers);

    /**
     * Removes the specified handler from this pipeline.
     *
     * @param  handler          the handler to remove
     *
     * @throws NoSuchElementException
     *         if there's no such handler in this pipeline
     * @throws NullPointerException
     *         if the specified handler is {@code null}
     */
    Pipeline<H, C> remove(H handler);

    /**
     * Removes the handler with the specified name from this pipeline.
     *
     * @param  name             the name under which the handler was stored.
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if there's no such handler with the specified name in this pipeline
     * @throws NullPointerException
     *         if the specified name is {@code null}
     */
    H remove(String name);

    /**
     * Removes the handler of the specified type from this pipeline.
     *
     * @param handlerType   the type of the handler
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if there's no such handler of the specified type in this pipeline
     * @throws NullPointerException
     *         if the specified handler type is {@code null}
     */
    <T extends H> T remove(Class<T> handlerType);

    /**
     * Removes the first handler in this pipeline.
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if this pipeline is empty
     */
    H removeFirst();

    /**
     * Removes the last handler in this pipeline.
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if this pipeline is empty
     */
    H removeLast();

    /**
     * Replaces the specified handler with a new handler in this pipeline.
     *
     * @param  oldHandler    the handler to be replaced
     * @param  newName       the name under which the replacement should be added
     * @param  newHandler    the handler which is used as replacement
     *
     * @return itself

     * @throws NoSuchElementException
     *         if the specified old handler does not exist in this pipeline
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     */
    Pipeline<H, C> replace(H oldHandler, String newName, H newHandler);

    /**
     * Replaces the handler of the specified name with a new handler in this pipeline.
     *
     * @param  oldName       the name of the handler to be replaced
     * @param  newName       the name under which the replacement should be added
     * @param  newHandler    the handler which is used as replacement
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if the handler with the specified old name does not exist in this pipeline
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     */
    H replace(String oldName, String newName, H newHandler);

    /**
     * Replaces the handler of the specified type with a new handler in this pipeline.
     *
     * @param  oldHandlerType   the type of the handler to be removed
     * @param  newName          the name under which the replacement should be added
     * @param  newHandler       the handler which is used as replacement
     *
     * @return the removed handler
     *
     * @throws NoSuchElementException
     *         if the handler of the specified old handler type does not exist
     *         in this pipeline
     * @throws IllegalArgumentException
     *         if a handler with the specified new name already exists in this
     *         pipeline, except for the handler to be replaced
     * @throws NullPointerException
     *         if the specified old handler or new handler is
     *         {@code null}
     */
    <T extends H> T replace(Class<T> oldHandlerType, String newName,
                                         H newHandler);

    /**
     * Returns the first handler in this pipeline.
     *
     * @return the first handler.  {@code null} if this pipeline is empty.
     */
    H first();

    /**
     * Returns the last handler in this pipeline.
     *
     * @return the last handler.  {@code null} if this pipeline is empty.
     */
    H last();

    /**
     * Returns the handler with the specified name in this
     * pipeline.
     *
     * @return the handler with the specified name.
     *         {@code null} if there's no such handler in this pipeline.
     */
    H get(String name);

    /**
     * Returns the handler of the specified type in this
     * pipeline.
     *
     * @return the handler of the specified handler type.
     *         {@code null} if there's no such handler in this pipeline.
     */
    <T extends H> T get(Class<T> handlerType);

    /**
     * Returns the {@link List} of the handler names.
     */
    List<String> names();

    /**
     * Converts this pipeline into an ordered {@link Map} whose keys are
     * handler names and whose values are handlers.
     */
    Map<String, H> toMap();

    /**
     * Returns the context of the first handler in this pipeline.
     *
     * @return the context of the first handler.  {@code null} if this pipeline is empty.
     */
    C firstContext();

    /**
     * Returns the context of the last handler in this pipeline.
     *
     * @return the context of the last handler.  {@code null} if this pipeline is empty.
     */
    C lastContext();

    /**
     * Returns the context object of the specified handler in
     * this pipeline.
     *
     * @return the context object of the specified handler.
     *         {@code null} if there's no such handler in this pipeline.
     */
    C context(H handler);

    /**
     * Returns the context object of the handler with the
     * specified name in this pipeline.
     *
     * @return the context object of the handler with the specified name.
     *         {@code null} if there's no such handler in this pipeline.
     */
    C context(String name);

    /**
     * Returns the context object of the handler of the
     * specified type in this pipeline.
     *
     * @return the context object of the handler of the specified type.
     *         {@code null} if there's no such handler in this pipeline.
     */
    C context(Class<? extends H> handlerType);
}
