package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorInput;
import org.osgi.framework.Bundle;
import org.xmind.core.IWorkbook;
import org.xmind.ui.internal.MindMapUIPlugin;

public class MME {

    private static Bundle ideBundle = null;

    private static boolean noIDE = false;

    public static IEditorInput createNonExistingEditorInput() {
        return new WorkbookEditorInput();
    }

    public static IEditorInput createNamedEditorInput(String name) {
        return new WorkbookEditorInput(name);
    }

    public static IEditorInput createTemplatedEditorInput(
            InputStream templateStream) {
        return new WorkbookEditorInput(null, templateStream);
    }

    public static IEditorInput createTemplatedEditorInput(String name,
            InputStream templateStream) {
        return new WorkbookEditorInput(name, templateStream);
    }

    public static IEditorInput createLoadedEditorInput(IWorkbook workbook) {
        return new WorkbookEditorInput(workbook);
    }

    public static IEditorInput createLoadedEditorInput(String name,
            IWorkbook workbook) {
        return new WorkbookEditorInput(name, workbook);
    }

    /**
     * Creates an editor input using the given file. Note that if there's
     * Eclipse IDE running, the result will be an
     * {@link org.eclipse.ui.ide.FileStoreEditorInput}; otherwise an instance of
     * {@link FileEditorInput} will be returned.
     * 
     * @param path
     *            The absolute path of a file
     * @return A new editor input representing the given file
     * @throws CoreException
     *             if the creation failed
     */
    public static IEditorInput createFileEditorInput(String path)
            throws CoreException {
        if (path == null)
            throw new IllegalArgumentException("Path is null"); //$NON-NLS-1$
        return createFileEditorInput(new File(path));
    }

    /**
     * Creates an editor input using the given file. Note that if there's
     * Eclipse IDE running, the result will be an
     * {@link org.eclipse.ui.ide.FileStoreEditorInput}; otherwise an instance of
     * {@link FileEditorInput} will be returned.
     * 
     * @param file
     *            The file
     * @return A new editor input representing the given file.
     * @throws CoreException
     *             if the creation failed
     */
    public static IEditorInput createFileEditorInput(File file)
            throws CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$

        Bundle ide = getIDE();
        if (ide != null) {
            IFileStore fileStore = EFS.getStore(file.toURI());
            return createFileEditorInput(ide, fileStore);
        }
        return new FileEditorInput(file);
    }

    /**
     * Creates an editor input using the given file store.
     * <p>
     * <b>IMPORTANT:</b> This method should ONLY be called when there's Eclipse
     * IDE in the runtime environment.
     * </p>
     * 
     * @param fileStore
     * @return
     * @throws CoreException
     *             if the creation failed
     * @throws IllegalStateException
     *             if the Eclipse IDE plugin isn't in the runtime environment
     */
    public static IEditorInput createFileEditorInput(IFileStore fileStore)
            throws CoreException {
        if (fileStore == null)
            throw new IllegalArgumentException("File store is null"); //$NON-NLS-1$

        Bundle ide = getIDE();
        if (ide != null) {
            return createFileEditorInput(ide, fileStore);
        }
        throw new IllegalStateException(
                "Can't create editor input using IFileStore when no Eclipse IDE plugin is resolved."); //$NON-NLS-1$
    }

    /*
     * We don't depend on org.eclipse.ui.ide, so we have to use class loader.
     */
    @SuppressWarnings("unchecked")
    private static IEditorInput createFileEditorInput(Bundle ide,
            IFileStore fileStore) throws CoreException {
        if (fileStore == null)
            throw new IllegalArgumentException("File store is null"); //$NON-NLS-1$

        try {
            Class clazz = ide
                    .loadClass("org.eclipse.ui.ide.FileStoreEditorInput"); //$NON-NLS-1$
            Constructor c = clazz.getConstructor(IFileStore.class);
            return (IEditorInput) c.newInstance(fileStore);
        } catch (Throwable e) {
            IStatus status = new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID,
                    "Unable to create FileStoreEditorInput."); //$NON-NLS-1$
            throw new CoreException(status);
        }
    }

    /**
     * Creates an editor input using the given file.
     * <p>
     * <b>IMPORTANT:</b> This method should ONLY be called when there's Eclipse
     * IDE in the runtime environment.
     * </p>
     * 
     * @param file
     *            The file
     * @return A new editor input representing the given file.
     * @throws CoreException
     *             if the creation failed
     * @throws IllegalStateException
     *             if the Eclipse IDE isn't in the runtime environment
     */
    public static IEditorInput createFileEditorInput(IFile file)
            throws CoreException {
        if (file == null)
            throw new IllegalArgumentException("File is null"); //$NON-NLS-1$
        Bundle ide = getIDE();
        if (ide != null) {
            return createFileEditorInput(ide, file);
        }
        throw new IllegalStateException(
                "Can't create editor input using IFile when no Eclipse IDE plugin is resolved."); //$NON-NLS-1$
    }

    /*
     * We don't depend on org.eclipse.ui.ide, so we have to use class loader.
     */
    @SuppressWarnings("unchecked")
    private static IEditorInput createFileEditorInput(Bundle ide, IFile file)
            throws CoreException {
        try {
            Class clazz = ide.loadClass("org.eclipse.ui.part.FileEditorInput"); //$NON-NLS-1$
            Constructor c = clazz.getConstructor(IFile.class);
            Object input = c.newInstance(file);
            return (IEditorInput) input;
        } catch (Throwable e) {
            IStatus status = new Status(IStatus.ERROR,
                    MindMapUIPlugin.PLUGIN_ID, NLS.bind(
                            "Failed to create IDE's FileEditorInput: {0}", file //$NON-NLS-1$
                                    .getFullPath()));
            throw new CoreException(status);
        }
    }

    public static File getFile(Object input) {
        IFileStore fileStore = getFileStore(input);
        if (fileStore != null) {
            try {
                return fileStore.toLocalFile(0, new NullProgressMonitor());
            } catch (CoreException ignore) {
            }
        }
        return null;
    }

    public static IFileStore getFileStore(Object input) {
        IFileStore fileStore = (IFileStore) getAdapter(input, IFileStore.class);
        if (fileStore == null) {
            File file = (File) getAdapter(input, File.class);
            if (file != null) {
                try {
                    fileStore = EFS.getStore(file.toURI());
                } catch (CoreException ignore) {
                }
            }
        }
        if (fileStore == null) {
            fileStore = forceFileStore(input);
        }
        return fileStore;
    }

    static Object getAdapter(Object adaptable, Class adapterType) {
        if (adaptable instanceof IAdaptable) {
            return ((IAdaptable) adaptable).getAdapter(adapterType);
        }
        return Platform.getAdapterManager().getAdapter(adaptable, adapterType);
    }

    @SuppressWarnings("unchecked")
    private static IFileStore forceFileStore(Object adaptable) {
        if (!(isInplementation(adaptable, "org.eclipse.ui.IURIEditorInput"))) //$NON-NLS-1$
            return null;

        Class clazz = adaptable.getClass();
        try {
            // For 'org.eclipse.ui.IURIEditorInput':
            // we don't directly depend on org.eclipse.ui.ide,
            // so we have to count on reflection.
            //FIXME design an extension point for this
            Method method = clazz.getMethod("getURI"); //$NON-NLS-1$
//            if (method.isAccessible()) {
            Object result = method.invoke(adaptable);
            if (result instanceof URI) {
                return EFS.getStore((URI) result);
            }
//            }
        } catch (Throwable ignore) {
        }
        return null;
    }

    private static boolean isInplementation(Object obj, String interfaceName) {
        Class[] interfaces = obj.getClass().getInterfaces();
        return hasInterface(interfaces, interfaceName);
    }

    private static boolean hasInterface(Class[] interfaces, String interfaceName) {
        if (interfaces.length == 0)
            return false;
        for (Class c : interfaces) {
            if (isClass(c, interfaceName)) {
                return true;
            }
            if (hasInterface(c.getInterfaces(), interfaceName))
                return true;
        }
        return false;
    }

    private static boolean isClass(Class c, String className) {
        return className.equals(c.getName());
    }

    private static Bundle getIDE() {
        if (ideBundle == null && !noIDE) {
            ideBundle = Platform.getBundle("org.eclipse.ui.ide"); //$NON-NLS-1$
            noIDE = (ideBundle == null);
        }
        return ideBundle;
    }

}
