package org.hesperides.domain.templatecontainers.entities;

import lombok.Value;
import org.hesperides.domain.templatecontainers.exceptions.RequiredPropertyCannotHaveDefaultValueException;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Value
public class Property extends AbstractProperty {

    boolean isRequired;
    String comment;
    String defaultValue;
    String pattern;
    boolean isPassword;

    public Property(String name, boolean isRequired, String comment, String defaultValue, String pattern, boolean isPassword) {
        super(name);
        this.isRequired = isRequired;
        this.comment = comment;
        this.defaultValue = defaultValue;
        this.pattern = pattern;
        this.isPassword = isPassword;
    }

    public void validate() {
        if (isRequired && !StringUtils.isEmpty(defaultValue)) {
            throw new RequiredPropertyCannotHaveDefaultValueException(getName());
        }
    }

    public enum Annotation {
        IS_REQUIRED("required"),
        COMMENT("comment"),
        DEFAULT_VALUE("default"),
        PATTERN("pattern"),
        IS_PASSWORD("password");

        private final String name;

        Annotation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Annotation fromName(String name) {
            Annotation result = null;
            for (Annotation annotation : Annotation.values()) {
                if (annotation.getName().equalsIgnoreCase(name)) {
                    result = annotation;
                    break;
                }
            }
            return result;
        }
    }

    private static final String NAME_ANNOTATIONS_SEPARATOR_REGEX = "[|]";
    private static final int NAME_INDEX = 0;
    private static final int ANNOTATIONS_INDEX = 1;
    private static final String ANNOTATION_PREFIX = "@";

    /**
     * Crée un objet Property à partir d'une chaîne de caractère au format suivant :
     * property_name[|@annotation[ annotation_value][ @annotation[ annotation_value]]*]
     * <p>
     * Et en Français :
     * <p>
     * La définition doit contenir au moins le nom de la variable, puis éventuellement un pipe "|" suivi d'une ou plusieurs annotations.
     * Les annotations qui peuvent être :
     * - @required
     * - @comment annotation_value (avec ou sans "")
     * - @default annotation_value (avec ou sans "")
     * - @pattern annotation_value (avec ou sans "")
     * - @password
     * <p>
     * Il est possible de remplir un commentaire sans mettre l'annotation @comment, à condition qu'elle soit la première.
     * De plus, par ce biais, un commentaire peut être composé d'espaces
     *
     * Une proprité ne peut avoir plusieurs fois la même annotation, sinon l'erreur ModelAnnotationException est levée
     * TODO ModelAnnotationException
     * @param propertyDefinition
     * @return
     */
    public static Property extractPropertyFromStringDefinition(String propertyDefinition) {
        Property property = null;
        if (propertyDefinition != null) {
            String[] propertyAttributes = propertyDefinition.split(NAME_ANNOTATIONS_SEPARATOR_REGEX);

            String name = propertyAttributes[NAME_INDEX].trim();
            boolean isRequired = false;
            String comment = "";
            String defaultValue = "";
            String pattern = "";
            boolean isPassword = false;

            if (propertyAttributes.length > 1) {
                String[] annotedProperties = propertyAttributes[ANNOTATIONS_INDEX].split(ANNOTATION_PREFIX);
                //Le split renvoit "" lorsqu'une propriété commence par une annotation avec @ après le pipe
                if(annotedProperties[0].equals("")){
                    annotedProperties = Arrays.copyOfRange(annotedProperties,1,annotedProperties.length);
                }
                int index = 0;
                boolean isFirstWordAnAnnotation = false;
                //Si il y a une chaine de caratère après le pipe, et sans annotations, c'est un commentaire
                for (Annotation a : Annotation.values()) {
                    if (annotedProperties[index].toLowerCase().startsWith(a.getName().toLowerCase())) {
                        isFirstWordAnAnnotation = true;
                    }
                }

                if (!isFirstWordAnAnnotation) {
                    comment = extractPropertyAnnotationValue(annotedProperties[index]);
                    index++;
                }

                for (int i = index; i < annotedProperties.length ; ++i) {
                    String annotation = annotedProperties[i];
                    if (annotation.toLowerCase().startsWith(Annotation.IS_REQUIRED.getName().toLowerCase())) {
                        isRequired = true;

                    } else if (annotation.toLowerCase().startsWith(Annotation.COMMENT.getName().toLowerCase())) {
                        if (!isFirstWordAnAnnotation) {
                            //TODO lever une exception ModelAnnotationException
                        }
                        else
                            comment = extractPropertyAnnotationValue(annotation);

                    } else if (annotation.toLowerCase().startsWith(Annotation.DEFAULT_VALUE.getName().toLowerCase())) {
                        defaultValue = extractPropertyAnnotationValue(annotation);

                    } else if (annotation.toLowerCase().startsWith(Annotation.PATTERN.getName().toLowerCase())) {
                        pattern = extractPropertyAnnotationValue(annotation);

                    } else if (annotation.toLowerCase().startsWith(Annotation.IS_PASSWORD.getName().toLowerCase())) {
                        isPassword = true;
                    }

                }
            }
            property = new Property(name, isRequired, comment, defaultValue, pattern, isPassword);
        }
        return property;
    }

    /**
     * Récupère la valeur entre le premier espace et la fin de la chaîne de caractère passée en paramètre
     */
    public static String extractPropertyAnnotationValue(String annotation) {
        int indexOfFirstSpace = annotation.indexOf(" ");
        String valueThatMayBeSurroundedByQuotes = annotation.substring(indexOfFirstSpace);
        return removeSurroundingQuotesIfPresent(valueThatMayBeSurroundedByQuotes.trim());
    }

    public static String removeSurroundingQuotesIfPresent(String value) {
        boolean startsWithQuotes = "\"".equals(value.substring(0, 1));
        boolean endsWithQuotes = "\"".equals(value.substring(value.length() - 1));
        if (startsWithQuotes && endsWithQuotes) {
            value = value.substring(1, value.length() - 1);
        }
        return value.trim();
    }
}
