# Potential dangers of XML: internal and external entities

XML is widely used even in its simplest form.
It is characterised by a tree structure a bit too verbose.
But there are some inherent dangers in XML that you must know 

## XML internal entities

An internal entity has following definition:
```xml
<!DOCTYPE test [
    <!ENTITY occurrence "replacement" > <!-- definition -->
]>
```

And can be used in the document
```xml
<text>&occurrence;</text> <!-- usage -->
```

As a result, the XML parser will replace every `&occurrence;` by `replacement`.
This is useful but sometimes a bit dangerous.

## XML external entities

An external entity has following definition:
```xml
<!DOCTYPE test [
    <!ENTITY occurrence SYSTEM "replacement.txt" > <!-- definition -->
]>
```

And can be used in the document
```xml
<text>&occurrence;</text> <!-- usage -->
```

As a result, the XML parser will replace every `&occurrence;` with the contents of `replacement.txt`.
This is useful but sometimes a bit dangerous.

## Attacks

The class `XmlDangersTests` contains three attacks based on internal and external entities:
* [Billion laughs attack](https://en.wikipedia.org/wiki/Billion_laughs_attack), a DoS attack aimed at parsers of XML documents.
It is an attack that triggers an exponential expansion of internal entities.
The Java 8 XML parser is able to handle this attack with a limit in the entity expansions per document (64000).
* [Quadratic blowup](https://en.wikipedia.org/wiki/Billion_laughs_attack), it is a quadratic variant of the above.
* [XXE attack](https://en.wikipedia.org/wiki/XML_external_entity_attack), can be used to craft an access to private information.


