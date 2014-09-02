package fr.univmobile.devel.xmldoclet;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.AnnotationTypeDoc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

import fr.univmobile.testutil.Dumper;
import fr.univmobile.testutil.XMLDumper;

/**
 * Custom doclet for XML generation.
 */
public class XMLDoclet {

	/**
	 * From Sun’s doclet API.
	 */
	public static boolean start(final RootDoc rootDoc) {

		/*
		 * System.out.println("options:");
		 * 
		 * for (final String[] option : rootDoc.options()) {
		 * 
		 * for (final String o : option) { System.out.print(" " + o);
		 * System.out.println(); } }
		 * 
		 * if (true) throw new NotImplementedException();
		 */

		// Basedir is: target/site/apidocs/

		final File destFile = new File("xmldoclet.xml");

		try {

			System.out.println("Dumping into: " + destFile.getCanonicalPath()
					+ "...");

			new XMLDoclet(destFile).dump(rootDoc).close();

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		return true;
	}

	private XMLDoclet(final File destFile) throws IOException {

		dumper = XMLDumper.newXMLDumper("rootDoc", destFile.getCanonicalFile());
	}

	private final Dumper dumper;

	public void close() throws IOException {

		dumper.close();
	}

	public XMLDoclet dump(final RootDoc rootDoc) throws IOException {

		for (final ClassDoc classDoc : rootDoc.classes()) {

			final Dumper classDumper = dumper
					.addElement("class")
					.addAttribute("name", classDoc.name())
					.addAttribute("simpleTypeName", classDoc.simpleTypeName())
					.addAttribute("qualifiedName", classDoc.qualifiedName())
					.addAttribute("containingPackage",
							classDoc.containingPackage().name()) //
					.addAttribute("isAbstract", classDoc.isAbstract()) //
					.addAttribute("isAnnotationType",
							classDoc.isAnnotationType()) //
					.addAttribute("isClass", classDoc.isClass()) //
					.addAttribute("isEnum", classDoc.isEnum()) //
					.addAttribute("isError", classDoc.isError()) //
					.addAttribute("isException", classDoc.isException()) //
					.addAttribute("isFinal", classDoc.isFinal()) //
					.addAttribute("isInterface", classDoc.isInterface()) //
					.addAttribute("isPackagePrivate",
							classDoc.isPackagePrivate()) //
					.addAttribute("isPublic", classDoc.isPublic()) //
					.addAttribute("isSerializable", classDoc.isSerializable());

			dumpAnnotations(classDumper, classDoc.annotations());

			for (final ClassDoc interfaceDoc : classDoc.interfaces()) {

				classDumper
						.addElement("interface")
						.addAttribute("name", interfaceDoc.name())
						.addAttribute("simpleTypeName",
								interfaceDoc.simpleTypeName())
						.addAttribute("qualifiedName",
								interfaceDoc.qualifiedName())
						.addAttribute("containingPackage",
								interfaceDoc.containingPackage().name());
			}

			@Nullable
			final ClassDoc superclassDoc = classDoc.superclass();

			if (superclassDoc != null) {
				classDumper
						.addElement("superclass")
						.addAttribute("name", superclassDoc.name())
						.addAttribute("simpleTypeName",
								superclassDoc.simpleTypeName())
						.addAttribute("qualifiedName",
								superclassDoc.qualifiedName())
						.addAttribute("containingPackage",
								superclassDoc.containingPackage().name());
			}

			for (final ConstructorDoc constructorDoc : classDoc.constructors()) {

				final Dumper constructorDumper = classDumper
						.addElement("constructor");

				dumpAnnotations(constructorDumper, constructorDoc.annotations());

				dumpParameters(constructorDumper, constructorDoc.parameters());
			}

			for (final MethodDoc methodDoc : classDoc.methods()) {

				final Type returnType = methodDoc.returnType();

				final Dumper methodDumper = classDumper
						.addElement("method")
						.addAttribute("name", methodDoc.name())
						.addElement("returnType")
						.addAttribute("isPrimitive", returnType.isPrimitive())
						.addAttribute("simpleTypeName",
								returnType.simpleTypeName())
						.addAttribute("dimension", returnType.dimension());

				dumpAnnotations(methodDumper, methodDoc.annotations());

				dumpParameters(methodDumper, methodDoc.parameters());
			}
		}

		return this;
	}

	private static void dumpParameters(final Dumper dumper,
			final Parameter[] parameters) throws IOException {

		for (final Parameter parameter : parameters) {

			final Type parameterType = parameter.type();

			final Dumper parameterDumper = dumper
					.addElement("parameter")
					.addAttribute("name", parameter.name())
					.addElement("type")
					.addAttribute("isPrimitive", parameterType.isPrimitive())
					.addAttribute("simpleTypeName",
							parameterType.simpleTypeName())
					.addAttribute("dimension", parameterType.dimension());

			dumpAnnotations(parameterDumper, parameter.annotations());

		}
	}

	private static void dumpAnnotations(final Dumper dumper,
			final AnnotationDesc[] annotationDescs) throws IOException {

		for (final AnnotationDesc annotationDesc : annotationDescs) {

			final AnnotationTypeDoc annotationType = annotationDesc
					.annotationType();

			dumper.addElement("annotation")
					.addAttribute("name", annotationType.name())
					.addAttribute("simpleTypeName",
							annotationType.simpleTypeName())
					.addAttribute("qualifiedName",
							annotationType.qualifiedName());
		}
	}
}
