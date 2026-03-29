package com.github.dimitryivaniuta.gateway.docstore.model;

/**
 * Enumerates logical file formats used by legacy composite Docstore keys.
 */
public enum DocumentFormat {

    /** Portable Document Format. */
    PDF,

    /** Microsoft Word document. */
    DOC,

    /** HyperText Markup Language. */
    HTML,

    /** Any other format not modelled yet. */
    OTHER
}
