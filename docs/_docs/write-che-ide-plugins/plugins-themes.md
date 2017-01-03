---
tags: [ "eclipse" , "che" ]
title: Themes
excerpt: "Theme API"
layout: docs
permalink: /:categories/themes/
---
{% include base.html %}
Themes API is required to be able to quickly change look and feel of the IDE, and have an easy way to change fonts, their color and the entire color scheme of IDE in a centralized way. As a result, with Themes API it has become possible to choose different color schemes and add new ones.

# Scope
In fact, Themes API is all about changing colors of IDE components without changing layout and forms of UI objects. Themes API rather supports color schemes, but not the entire UI and its components in a broad sense. In Themes API it has become possible to change color scheme for syntax highlighting which is thought to be one of potentially most typical use cases.
So, if a user needs to change syntax highlighting there are two ways to do it:
* Create own color schemes
* Use tools to configure just syntax highlighting, editor background, fonts etc. (possible, but not implemented yet)

Theme is related to user account and is stored as part of user account settings.

# Limitations
As said above, it is possible to configure colors for UI elements, as well as change types and size of fonts. It is impossible to change the look and layout of UI elements, like changing the look of a button or a dialogue box (its geometrical shape etc.)

# How to Use Theme API
There are basically two scenarios possible:
* Adding own theme
* Using existing Theme API in own custom theme components.

## Adding Own Theme
To add a new theme one needs to implement the interface `org.eclipse.che.ide.api.Theme`, and register it in a `ThemeAgent`.
This is how it's done:
```java  
void addTheme(@NotNull Theme theme);\
```
Alternatively, it is possible just to extend an existing theme (rather than implementing own). However, in this case it will still be a new Theme which will be based on an existing one. In such a way, users will just change colors they need to change. Of course, if the goal is a total theme overhaul then the work should be done from the ground up (i.e. own implementation).

## Using Theme API in Own Custom Components
To be able to use Themes API one needs to be familiar with `CssResource`, where runtime substitution is used:
```java  
public interface CoreCss extends CssResource {
  public interface Resources extends ClientBudnle {
    @Source({"Core.css\ "org/eclipse/che/ide/api/ui/style.css"})
    CoreCss coreCss();
  }
}
```
In the above example, we ensure that variables declared in `style.css` (provided by Themes API) are visible in a custom CSS `Core.css`. For example, if in your custom CSS file you need to use a default font family, font color and font size, this is how it will look like:
```css  
textarea {
  font-family: mainFontFamily;
  color: mainFontColor;
  background-color: inputBackground;
  border: 1px solid tabBorder;
  border-radius: 2px;
  font-size: fontSize;
}\
```
Here, `mainFontColor`, `mainFontFamily` and `mainFontSize` are declared in `style.css` provided by Themes API, and thus can be re-used in a custom CSS file.

# Extending Dark Theme
SDK users can easily change looks and feel of the product by adding own themes or extending existing themes developed by Che contributors.
To get started with creating your own theme based on the existing one, let's extend Dark Theme which is provided in API.
```java  
public class DarkThemeExt extends DarkTheme\
```
Next, we override Theme ID and name:
```java  
@Override
public String getId() {
  return "new theme id";
}

@Override
public String getDescription() {
  return "New extended dark theme";
}
```
A new name will be displayed in the list of available themes at Window > Preferences > Themes.
Now, let's override colors for main font and some panel background:
```java  
@Override
public String getMainFontColor() {
  return "red";
}

@Override
public String getPartBackground() {
  return "white";
}

@Override
public String getTabsPanelBackground() {
  return "white";
}
```
# Register Own Theme Using GIN Multibinding
Let's register a new theme:
```java  
GinMultibinder themeBinder = GinMultibinder.newSetBinder(binder(), Theme.class);
themeBinder.addBinding().to(DarkThemeExt.class);\
```
Having tested your new theme (you can update source code and update extension in runtime), you can add it to Che bundle, so that it loads along with all other extensions when starting.
