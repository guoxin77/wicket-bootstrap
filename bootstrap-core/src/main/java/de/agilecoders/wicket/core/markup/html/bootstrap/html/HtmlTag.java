package de.agilecoders.wicket.core.markup.html.bootstrap.html;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.markup.html.references.ModernizrJavaScriptReference;
import de.agilecoders.wicket.core.util.CssClassNames;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.TransparentWebMarkupContainer;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.request.WebClientInfo;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * A {@link HtmlTag} is a {@link TransparentWebMarkupContainer} that adds some attributes
 * to the pages html tag. It will add a "lang" attribute, which value is provided by
 * given {@code locale}, in a short form: {@code Locale.ENGLISH = "en-en"}. Also it
 * will add a "class" attribute that contains special css marker like active theme, browser
 * identifier and a "no-js" flag which is used by modernizr to detect whether javascript is
 * available or not.
 *
 * <h3>Usage:</h3>
 *
 * <pre lang="java">
 *      class MyPage extends Page {
 *          public MyPage(PageParameters params) {
 *              // with chrome browser and bootstrap theme
 *              add(new HtmlTag("html-tag")); // <html lang="en-en" class="chrome22 bootstrap"
 *              // or with modernizr and different locale
 *              add(new HtmlTag("html-tag", Locale.GERMAN, true)); // <html lang="de-de" class="chrome22 bootstrap no-js"
 *              // or with an IE7
 *              add(new HtmlTag("html-tag", Locale.GERMAN, true)); // <html lang="de-de" class="ie7 lt-ie8 lt-ie9 bootstrap no-js"
 *          }
 *      }
 * </pre>
 * <pre lang="html">
 *      <!DOCTYPE html>
 *          <html wicket:id="html-tag">
 *              <head>
 *                  [...]
 *              </head>
 * </pre>
 *
 * @author miha
 */
public class HtmlTag extends TransparentWebMarkupContainer {
    private final boolean useModernizr;
    private final Locale locale;
    private final ClientProperties clientProperties;

    /**
     * Construct.
     *
     * @param markupId     The component' markup id
     * @param locale       locale to use for "lang" attribute
     * @param useModernizr whether to use modernizr or not
     */
    public HtmlTag(final String markupId, final Locale locale, final boolean useModernizr) {
        super(markupId);

        this.locale = locale;
        this.useModernizr = useModernizr;
        this.clientProperties = newWebClientInfo();
    }

    /**
     * Construct.
     * Uses {@link Locale#ENGLISH} as default locale
     *
     * @param markupId     the component id
     * @param useModernizr whether to use modernizr or not
     */
    public HtmlTag(final String markupId, final boolean useModernizr) {
        this(markupId, Locale.ENGLISH, useModernizr);
    }

    /**
     * Construct.
     * Doesn't add modernizr support.
     *
     * @param markupId the component id
     * @param locale   locale to use for "lang" attribute
     */
    public HtmlTag(final String markupId, final Locale locale) {
        this(markupId, locale, false);
    }

    /**
     * Construct.
     * Uses {@link Locale#ENGLISH} as default locale and doesn't add modernizr support.
     *
     * @param markupId the component id
     */
    public HtmlTag(final String markupId) {
        this(markupId, Locale.ENGLISH, false);
    }

    /**
     * @return new {@link WebClientInfo} instance
     */
    protected ClientProperties newWebClientInfo() {
        return new WebClientInfo(getRequestCycle()).getProperties();
    }

    /**
     * transforms a locale to an attribute value
     *
     * @param locale current locale
     * @return locale as attribute
     */
    private String toAttributeValue(final Locale locale) {
        return locale.toLanguageTag(); // IETF BCP 47
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        super.onComponentTag(tag);

        checkComponentTag(tag, "html");

        if (locale != null) {
            tag.put("lang", toAttributeValue(locale));
        }

        final CssClassNames.Builder cssClassNames = CssClassNames.newBuilder();
        if (useModernizr) {
            cssClassNames.add("no-js");
        }

        cssClassNames.add(createBrowserShortcut(clientProperties));
        cssClassNames.add("theme-" + Bootstrap.getSettings().getActiveThemeProvider().getActiveTheme().name());

        tag.put("class", cssClassNames.asString());
    }

    /**
     * creates a browser shortcuts to identify old IE versions.
     *
     * @param clientProperties current client properties
     * @return a set of browser shortcuts
     */
    private Set<String> createBrowserShortcut(final ClientProperties clientProperties) {
        Set<String> shortcut = new HashSet<>();

        if (clientProperties.isBrowserInternetExplorer()) {
            if (clientProperties.getBrowserVersionMajor() < 9) {
                shortcut.add("lt-ie9");

                if (clientProperties.getBrowserVersionMajor() < 8) {
                    shortcut.add("lt-ie8");

                    if (clientProperties.getBrowserVersionMajor() < 7) {
                        shortcut.add("lt-ie7");
                    }
                }
            }
        }

        return shortcut;
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);

        if (useModernizr) {
            response.render(newModernizrHeaderItem());
        }
    }

    /**
     * @return modernizr javascript reference as header item
     */
    protected HeaderItem newModernizrHeaderItem() {
        return JavaScriptHeaderItem.forReference(ModernizrJavaScriptReference.instance());
    }
}
