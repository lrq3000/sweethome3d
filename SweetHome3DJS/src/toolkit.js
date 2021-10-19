/*
 * toolkit.js
 *
 * Sweet Home 3D, Copyright (c) 2020 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

// Requires UserPreferences.js

/**
 * The root class for additional UI components.
 * @param {UserPreferences} preferences the current user preferences
 * @param {string|HTMLElement} template template element (view HTML will be this element's innerHTML) 
 *     or HTML string (if null or undefined, then the component creates an empty div for the root node)
 * @param {boolean} [useElementAsRootHTMLElement]
 * @constructor
 * @author Renaud Pawlak
 * @author Emmanuel Puybaret
 */
function JSComponent(preferences, template, useElementAsRootHTMLElement) {
  this.preferences = preferences;

  if (template instanceof HTMLElement && useElementAsRootHTMLElement === true) {
    this.container = template;
  } else {
    var html = "";
    if (template != null) {
      html = typeof template == "string" ? template : template.innerHTML;
    }
    this.container = document.createElement("div");
    this.container.innerHTML = this.buildHtmlFromTemplate(html);
  }
}

/**
 * Returns true if element is or is child of candidateParent, false otherwise.
 * @param {HTMLElement} element
 * @param {HTMLElement} candidateParent
 * @return {boolean}
 */
JSComponent.isElementContained = function(element, candidateParent) {
  if (element == null || candidateParent == null) {
    return false;
  }

  var currentParent = element;
  do {
    if (currentParent == candidateParent) {
      return true;
    }
  } while (currentParent = currentParent.parentElement);

  return false;
}

/**
 * Substitutes all the place holders in the html with localized labels.
 */
JSComponent.substituteWithLocale = function(preferences, html) {
  return html.replace(/\@\{([a-zA-Z0-9_.]+)\}/g, function(fullMatch, str) {
      var replacement = ResourceAction.getLocalizedLabelText(preferences, str.substring(0, str.indexOf('.')), str.substring(str.indexOf('.') + 1));
      return replacement || all;
    });
}

JSComponent.prototype.buildHtmlFromTemplate = function(templateHtml) {
  return JSComponent.substituteWithLocale(this.preferences, templateHtml);
}

/**
 * Returns the HTML element used to view this component.
 */
JSComponent.prototype.getHTMLElement = function() {
  return this.container;
}

/**
 * Attaches the given component to a child DOM element, becoming a child component.
 * @param {string} name the component's name, which matches child DOM element name (as defined in {@link JSComponent#getElement})
 * @param {JSComponent} component child component instance
 */
JSComponent.prototype.attachChildComponent = function(name, component) {
  this.getElement(name).appendChild(component.getHTMLElement());
}

/**
 * Registers given listener on given elements(s) and removes them when this component is disposed.
 * @param {(HTMLElement[]|HTMLElement)} elements
 * @param {string} eventName
 * @param {function} listener
 */
JSComponent.prototype.registerEventListener = function(elements, eventName, listener) {
  if (elements == null) {
    return;
  }
  if (elements instanceof NodeList || elements instanceof HTMLCollection) {
    var array = new Array(elements.length);
    for (var i = 0; i < elements.length; i++) {
      array[i] = elements[i];
    }
    elements = array;
  }
  if (!Array.isArray(elements)) {
    elements = [elements];
  }
  if (this.listeners == null) {
    this.listeners = [];
  }
  for (var i = 0; i < elements.length; i++) {
    var element = elements[i];
    element.addEventListener(eventName, listener, true);
  }
  this.listeners.push(
      {
        listener: listener,
        eventName: eventName,
        elements: elements
      });
}

/**
 * Releases all listeners registered with {@link JSComponent#registerEventListener}
 * @private
 */
JSComponent.prototype.unregisterEventListeners = function() {
  if (Array.isArray(this.listeners)) {
    for (var i = 0; i < this.listeners.length; i++) {
      var registeredEntry = this.listeners[i];
      for (var j = 0; j < registeredEntry.elements.length; j++) {
        var element = registeredEntry.elements[j];
        element.removeEventListener(registeredEntry.eventName, registeredEntry.listener);
      }
    }
  }
}

/**
 * Returns the named element that corresponds to the given name within this component.
 * A named element shall define the "name" attribute (for instance an input), or
 * a "data-name" attribute if the name attribue is not supported.
 */
JSComponent.prototype.getElement = function(name) {
  var element = this.container.querySelector("[name='" + name + "']");
  if (element == null) {
    element = this.container.querySelector("[data-name='" + name + "']");
  }
  return element;
}

/**
 * Returns the element that matches the given query selector within this component.
 * @param {string} query css selector to be applied on children elements
 */
JSComponent.prototype.findElement = function(query) {
  return this.container.querySelector(query);
}

/**
 * Returns the elements that match the given query selector within this component.
 * @param {string} query css selector to be applied on children elements
 */
JSComponent.prototype.findElements = function(query) {
  return this.container.querySelectorAll(query);
}

/**
 * Releases any resource or listener associated with this component, when it's disposed. 
 * Override to perform custom clean.
 * Don't forget to call super method: JSComponent.prototype.dispose()
 */
JSComponent.prototype.dispose = function() {
  this.unregisterEventListeners();
}

/**
 * Delegates to ResourceAction.getLocalizedLabelText(this.preferences, ...).
 * @param {Object} resourceClass
 * @param {string} propertyKey
 * @param {Array} resourceParameters
 * @return {string}
 * @protected
 */
JSComponent.prototype.getLocalizedLabelText = function(resourceClass, propertyKey, resourceParameters) {
  return ResourceAction.getLocalizedLabelText(this.preferences, resourceClass, propertyKey, resourceParameters);
}

/**
 * @param {string} value option's value
 * @param {string} text option's display text
 * @param {boolean} [selected] true if selected, default false
 * @return {HTMLOptionElement}
 */
JSComponent.createOptionElement = function(value, text, selected) {
  var option = document.createElement("option");
  option.value = value;
  option.textContent = text;
  if (selected !== undefined) {
    option.selected = selected;
  }
  return option;
}


/**
 * A class to create dialogs.
 * @param preferences      the current user preferences
 * @param {string} title the dialog's title (may contain HTML)
 * @param {string|HTMLElement} template template element (view HTML will be this element's innerHTML) or HTML string (if null or undefined, then the component creates an empty div
 * for the root node)
 * @param {{applier: function(JSDialog), disposer: function(JSDialog), size?: "small"|"medium"|"default"}} [behavior]
 * - applier: an optional dialog application function
 * - disposer: an optional dialog function to release associated resources, listeners, ...
 * - size: override style with "small" or "medium"
 * @constructor
 * @author Renaud Pawlak
 */
function JSDialog(preferences, title, template, behavior) {
  JSComponent.call(this, preferences, template, behavior);

  var dialog = this;
  if (behavior != null) {
    this.applier = behavior.applier;
    this.disposer = behavior.disposer;
  }

  this.getHTMLElement().classList.add("dialog-container");
  if (behavior.size) {
    this.getHTMLElement().classList.add(behavior.size);
  }
  this.getHTMLElement()._dialogBoxInstance = this;

  document.body.appendChild(this.getHTMLElement());

  if (title != null) {
    this.setTitle(title);
  }

  this.getCloseButton().addEventListener("click", function() {
      dialog.cancel();
    });

  this.buttonsPanel = this.findElement(".dialog-buttons");
  if (OperatingSystem.isMacOSX()) {
    this.buttonsPanel.classList.add("mac");
  }
  this.appendButtons(this.buttonsPanel);
  this.getHTMLElement().classList.add('buttons-' + this.buttonsPanel.querySelectorAll('button').length);
}
JSDialog.prototype = Object.create(JSComponent.prototype);
JSDialog.prototype.constructor = JSDialog;

/**
 * Appends dialog buttons to given panel.
 * @param {HTMLElement} buttonsPanel Dialog buttons panel
 * @protected
 */
JSDialog.prototype.appendButtons = function(buttonsPanel) {
  var html;
  if (this.applier) {
    html = "<button class='dialog-ok-button'>@{OptionPane.okButton.textAndMnemonic}</button>"
         + "<button class='dialog-cancel-button'>@{OptionPane.cancelButton.textAndMnemonic}</button>";
  } else {
    html = "<button class='dialog-cancel-button'>@{InternalFrameTitlePane.closeButtonAccessibleName}</button>";
  }
  buttonsPanel.innerHTML = JSComponent.substituteWithLocale(this.preferences, html);

  var dialog = this;

  var cancelButton = this.findElement(".dialog-cancel-button");
  if (cancelButton) {
    this.registerEventListener(cancelButton, "click", function(ev) {
        dialog.cancel();
      });
  }
  var okButton = this.findElement(".dialog-ok-button");
  if (okButton) {
    this.registerEventListener(okButton, "click", function(ev) {
        dialog.validate();
      });
  }
};

/**
 * Closes currently displayed topmost dialog if any.
 * @static
 */
JSDialog.closeTopMostDialogIfAny = function() {
  var topMostDialog = JSDialog.getTopMostDialog();
  if (topMostDialog != null) {
    topMostDialog.close();
  }
}

/**
 * Returns the currently displayed topmost dialog if any.
 * @return {JSDialog} currently displayed topmost dialog if any, otherwise null
 * @static
 */
JSDialog.getTopMostDialog = function() {
  var visibleDialogElements = document.querySelectorAll(".dialog-container.visible");
  if (visibleDialogElements.length > 0) {
    var topMostDialog = null;
    for (var i = 0; i < visibleDialogElements.length; i++) {
      var visibleDialog = visibleDialogElements[i]._dialogBoxInstance;
      if (topMostDialog == null || topMostDialog.displayIndex <= visibleDialog.displayIndex) {
        topMostDialog = visibleDialog;
      }
    }
  }
  return topMostDialog;
}

JSDialog.prototype.buildHtmlFromTemplate = function(templateHtml) {
  return JSComponent.substituteWithLocale(this.preferences,
      '<div class="dialog-content">' +
      '  <div class="dialog-top">' +
      '    <span class="title"></span>' +
      '    <span class="dialog-close-button">&times;</span>' +
      '  </div>' +
      '  <div class="dialog-body">' +
      JSComponent.prototype.buildHtmlFromTemplate.call(this, templateHtml) +
      '  </div>' +
      '  <div class="dialog-buttons">' +
      '  </div>' +
      '</div>');
}

/**
 * Returns the input that corresponds to the given name within this dialog.
 */
JSDialog.prototype.getInput = function(name) {
  return this.getHTMLElement().querySelector("[name='" + name + "']");
}

/**
 * Returns the close button of this dialog.
 */
JSDialog.prototype.getCloseButton = function() {
  return this.getHTMLElement().querySelector(".dialog-close-button");
}

/**
 * Called when the user presses the OK button.
 * Override to implement custom behavior when the dialog is validated by the user.
 */
JSDialog.prototype.validate = function() {
  if (this.applier != null) {
    this.applier(this);
  }
  this.close();
}

/**
 * Called when the user closes the dialog with no validation.
 */
JSDialog.prototype.cancel = function() {
  this.close();
}

/**
 * Closes the dialog and discard the associated DOM.
 */
JSDialog.prototype.close = function() {
  this.getHTMLElement().classList.add("closing");
  var dialog = this;
  // Let 500ms before releasing the dialog so that the closing animation can apply
  setTimeout(function() {
      dialog.getHTMLElement().classList.remove("visible");
      dialog.dispose();
      if (dialog.getHTMLElement() && document.body.contains(dialog.getHTMLElement())) {
        document.body.removeChild(dialog.getHTMLElement());
      }
    }, 500);
}

/**
 * Releases any resource or listener associated with this component, when it's disposed. 
 * Override to perform custom clean - Don't forget to call super.dispose().
 */
JSDialog.prototype.dispose = function() {
  JSComponent.prototype.dispose.call(this);
  if (typeof this.disposer == "function") {
    this.disposer(this);
  }
}

/**
 * Sets dialog title
 * @param {string} title
 */
JSDialog.prototype.setTitle = function(title) {
  var titleElement = this.findElement(".dialog-top .title");
  titleElement.textContent = JSComponent.substituteWithLocale(this.preferences, title || "");
}

/**
 * @return {boolean} true if this dialog is currently shown, false otherwise
 */
JSDialog.prototype.isDisplayed = function() {
  return this.getHTMLElement().classList.contains("visible");
}

/**
 * Default implementation of the DialogView.displayView function.
 */
JSDialog.prototype.displayView = function(parentView) {
  var dialog = this;

  this.getHTMLElement().style.display = "block";

  // Force browser to refresh before adding visible class to allow transition on width and height
  setTimeout(function() {
      dialog.getHTMLElement().classList.add("visible");
      dialog.displayIndex = JSDialog.shownDialogsCounter++;
      var focusedInput = dialog.findElement('input');
      if (focusedInput != null) {
        focusedInput.focus();
      }
    }, 100);
}

JSDialog.shownDialogsCounter = 0;


/**
 * A class to create wizard dialogs.
 * @param {UserPreferences} preferences the current user preferences
 * @param {WizardController} controller wizard's controller
 * @param {string} title the dialog's title (may contain HTML)
 * @param {string|HTMLElement} template template element (view HTML will be this element's innerHTML) or HTML string (if null or undefined, then the component creates an empty div
 * for the root node)
 * @param {{applier: function(JSDialog), disposer: function(JSDialog)}} [behavior]
 * - applier: an optional dialog application function
 * - disposer: an optional dialog function to release associated resources, listeners, ...
 * @constructor
 * @author Louis Grignon
 */
function JSWizardDialog(controller, preferences, title, behavior) {
  this.controller = controller;

  JSDialog.call(this, preferences, title,
    '<div class="wizard">' +
    '  <div stepIcon></div>' +
    '  <div stepView></div>' +
    '</div>',
    behavior);

  this.stepIconPanel = this.findElement("[stepIcon]");
  this.stepViewPanel = this.findElement("[stepView]");

  var dialog = this;
  this.updateStepView();
  controller.addPropertyChangeListener("STEP_VIEW", function(ev) {
      dialog.updateStepView();
    });

  this.updateStepIcon();
  controller.addPropertyChangeListener("STEP_ICON", function(ev) {
      dialog.updateStepIcon();
    });
  
  controller.addPropertyChangeListener("TITLE", function(ev) {
      dialog.setTitle(controller.getTitle());
    });
}
JSWizardDialog.prototype = Object.create(JSDialog.prototype);
JSWizardDialog.prototype.constructor = JSWizardDialog;

/**
 * Append dialog buttons to given panel
 * @param {HTMLElement} buttonsPanel Dialog buttons panel
 * @protected
 */
JSWizardDialog.prototype.appendButtons = function(buttonsPanel) {
  var cancelButton = "<button class='wizard-cancel-button'>@{InternalFrameTitlePane.closeButtonAccessibleName}</button>";
  var backButton = "<button class='wizard-back-button'>@{WizardPane.backOptionButton.text}</button>";
  var nextButton = "<button class='wizard-next-button'></button>";
  var buttons = "<div class='dialog-buttons'>" 
      + (OperatingSystem.isMacOSX() ? nextButton + backButton : backButton + nextButton) 
      + cancelButton + "</div>";
  buttonsPanel.innerHTML = JSComponent.substituteWithLocale(this.preferences, buttons);

  this.cancelButton = this.findElement(".wizard-cancel-button");
  this.backButton = this.findElement(".wizard-back-button");
  this.nextButton = this.findElement(".wizard-next-button");

  var dialog = this;
  var controller = this.controller;
  this.registerEventListener(this.cancelButton, "click", function(ev) {
      dialog.cancel();
    });

  this.backButton.disabled = !controller.isBackStepEnabled();
  controller.addPropertyChangeListener("BACK_STEP_ENABLED", function(ev) {
      dialog.backButton.disabled = !controller.isBackStepEnabled();
    });

  this.nextButton.disabled = !controller.isNextStepEnabled();
  controller.addPropertyChangeListener("NEXT_STEP_ENABLED", function(ev) {
      dialog.nextButton.disabled = !controller.isNextStepEnabled();
    });

  this.updateNextButtonText();
  controller.addPropertyChangeListener("LAST_STEP", function(ev) {
      dialog.updateNextButtonText();
    });

  this.registerEventListener(this.backButton, "click", function(ev) {
      controller.goBackToPreviousStep();
    });
  this.registerEventListener(this.nextButton, "click", function(ev) {
      if (controller.isLastStep()) {
        controller.finish();
        if (dialog != null) {
          dialog.validate();
        }
      } else {
        controller.goToNextStep();
      }
    });
};

/**
 * Change text of the next button depending on if state is last step or not
 * @private
 */
JSWizardDialog.prototype.updateNextButtonText = function() {
  this.nextButton.innerText = this.getLocalizedLabelText("WizardPane",
      this.controller.isLastStep()
          ? "finishOptionButton.text"
          : "nextOptionButton.text");
}

/**
 * Updates UI for current step.
 * @private
 */
JSWizardDialog.prototype.updateStepView = function() {
  var stepView = this.controller.getStepView();
  this.stepViewPanel.innerHTML = "";
  this.stepViewPanel.appendChild(stepView.getHTMLElement());
}

/**
 * Updates image for current step.
 * @private
 */
JSWizardDialog.prototype.updateStepIcon = function() {
  var iconPanel = this.stepIconPanel;
  iconPanel.innerHTML = "";
  // Add new icon
  var stepIcon = this.controller.getStepIcon();
  if (stepIcon != null) {
    var backgroundColor1 = "rgb(163, 168, 226)";
    var backgroundColor2 = "rgb(80, 86, 158)";
    try {
      // Read gradient colors used to paint icon background
      var stepIconBackgroundColors = this.getLocalizedLabelText(
          "WizardPane", "stepIconBackgroundColors").trim().split(" ");
      backgroundColor1 = stepIconBackgroundColors[0];
      if (stepIconBackgroundColors.length == 1) {
        backgroundColor2 = backgroundColor1;
      } else if (stepIconBackgroundColors.length == 2) {
        backgroundColor2 = stepIconBackgroundColors[1];
      }
    } catch (ex) {
      // Do not change if exception
    }

    var gradientColor1 = backgroundColor1;
    var gradientColor2 = backgroundColor2;
    iconPanel.style.background = "linear-gradient(180deg, " + gradientColor1 + " 0%, " + gradientColor2 + " 100%)";
    iconPanel.style.border = "solid 1px #333333";
    var icon = new Image();
    icon.onload = function() {
      if (iconPanel.clientHeight > 0) {
        icon.style.marginTop = ((iconPanel.clientHeight - icon.height) / 2) + "px";
      } else {
        icon.style.marginTop = '50%';
      }
      iconPanel.appendChild(icon);
    };
    icon.src = "lib/" + stepIcon;
  }
}


/**
 * Class handling context menus.
 * @param {UserPreferences} preferences the current user preferences
 * @param {HTMLElement|HTMLElement[]} sourceElements context menu will show when right click on this element. 
 *        Cannot be null for the root node
 * @param {function(JSPopupMenu.Builder, HTMLElement)}  build 
 *    Function called with a builder, and optionally with source element (which was right clicked, to show this menu)
 * @constructor
 * @author Louis Grignon
 * @author Renaud Pawlak
 */
function JSPopupMenu(preferences, sourceElements, build) {
  if (sourceElements == null || sourceElements.length === 0) {
    throw new Error("Cannot register a context menu on an empty list of elements");
  }
  this.sourceElements = sourceElements;
  if (!Array.isArray(sourceElements)) {
    this.sourceElements = [sourceElements];
  }

  this.build = build;

  JSComponent.call(this, preferences, "");
  this.getHTMLElement().classList.add("popup-menu");

  document.body.appendChild(this.getHTMLElement());

  var contextMenu = this;
  this.registerEventListener(sourceElements, "contextmenu", function(ev) {
      ev.preventDefault();
      if (JSPopupMenu.current != null) {
        JSPopupMenu.current.close();
      }
      contextMenu.showForSourceElement(this, ev);
    });
}
JSPopupMenu.prototype = Object.create(JSComponent.prototype);
JSPopupMenu.prototype.constructor = JSPopupMenu;

/**
 * Closes currently displayed context menu if any.
 * @static
 * @private
 */
JSPopupMenu.closeCurrentIfAny = function() {
  if (JSPopupMenu.current != null) {
    JSPopupMenu.current.close();
    return true;
  }
  return false;
}

/**
 * @param {HTMLElement} sourceElement
 * @param {Event} ev
 * @private
 */
JSPopupMenu.prototype.showForSourceElement = function(sourceElement, ev) {
  this.listenerUnregisterCallbacks = [];

  var builder = new JSPopupMenu.Builder();
  this.build(builder, sourceElement);

  var items = builder.items;
  // Remove last element if it is a separator
  if (items.length > 0 && items[items.length - 1] == JSPopupMenu.CONTEXT_MENU_SEPARATOR_ITEM) {
    items.pop();
  }
  var menuElement = this.createMenuElement(items);

  this.getHTMLElement().appendChild(menuElement);

  // Temporarily use hidden visibility to get element's height
  this.getHTMLElement().style.visibility = "hidden";
  this.getHTMLElement().classList.add("visible");

  // Adjust top/left and display
  var anchorX = ev.clientX;
  if (menuElement.clientWidth > window.innerWidth) {
    anchorX = 0;
  } else if (anchorX + menuElement.clientWidth + 20 > window.innerWidth) {
    anchorX = Math.max(0, window.innerWidth - menuElement.clientWidth - 20);
  }
  var anchorY = ev.clientY;
  if (menuElement.clientHeight > window.innerHeight) {
    anchorY = 0;
  } else if (anchorY + menuElement.clientHeight > window.innerHeight) {
    anchorY = window.innerHeight - menuElement.clientHeight;
  }

  this.getHTMLElement().style.visibility = "visible";
  this.getHTMLElement().style.left = anchorX + "px";
  this.getHTMLElement().style.top = anchorY + "px";

  JSPopupMenu.current = this;
};

/**
 * @param {{}[]} items same type as JSPopupMenu.Builder.items
 * @param {number} [zIndex] default to initial value: 1000
 * @return {HTMLElement} menu root html element (`<ul>`)
 * @private
 */
JSPopupMenu.prototype.createMenuElement = function(items, zIndex) {
  if (zIndex === undefined) {
    zIndex = 1000;
  }

  var menuElement = document.createElement("ul");
  menuElement.classList.add("items");
  menuElement.style.zIndex = zIndex;

  var backElement = document.createElement("li");
  backElement.classList.add("item");
  backElement.classList.add("back");
  backElement.textContent = "×";
  this.registerEventListener(backElement, "click", function(ev) {
      var isRootMenu = menuElement.parentElement.tagName.toLowerCase() != "li";
      if (isRootMenu) {
        JSPopupMenu.closeCurrentIfAny();
      } else {
        menuElement.classList.remove("visible");
      }
    });
  menuElement.appendChild(backElement);

  for (var i = 0; i < items.length; i++) {
    var item = items[i];

    var itemElement = document.createElement("li");
    if (item == JSPopupMenu.CONTEXT_MENU_SEPARATOR_ITEM) {
      itemElement.classList.add("separator");
    } else {
      this.initMenuItemElement(itemElement, item, zIndex);
    }

    menuElement.appendChild(itemElement)
  }

  return menuElement;
};

/**
 * Initializes a menu item element for the given item descriptor (model).
 * @param {HTMLElement} menuItemElement
 * @param {{}[]} item an item from JSPopupMenu.Builder.items
 * @param {number} zIndex current menu z-index
 * @private
 */
JSPopupMenu.prototype.initMenuItemElement = function(itemElement, item, zIndex) {
  var contextMenu = this;

  var itemIconElement = document.createElement("img");
  if (item.iconPath != null) {
    itemIconElement.src = item.iconPath;
    itemIconElement.classList.add("visible");
  }

  if (item.mode !== undefined) {
    itemElement.classList.add("checkable");
    if (item.selected === true) {
      itemElement.classList.add("selected");
    }
    if (item.iconPath == null) {
      itemIconElement = document.createElement("span");
      itemIconElement.innerHTML = item.selected === true ? "✓" : "&nbsp;";
      itemIconElement.classList.add("visible");
    }
  }

  var itemLabelElement = document.createElement("span");
  itemLabelElement.textContent = JSComponent.substituteWithLocale(this.preferences, item.label);

  itemElement.classList.add("item");
  itemElement.dataset["uid"] = item.uid;

  itemIconElement.classList.add("icon");
  itemElement.appendChild(itemIconElement);
  itemElement.appendChild(itemLabelElement);
  if (Array.isArray(item.subItems)) {
    itemElement.classList.add("sub-menu");

    var subMenuElement = this.createMenuElement(item.subItems, zIndex + 1);
    this.registerEventListener(itemElement, "click", function(ev) {
        subMenuElement.classList.add("visible");
      });
    this.registerEventListener(itemElement, "mouseover", function(ev) {
        var itemRect = itemElement.getBoundingClientRect();
        subMenuElement.style.position = "fixed";
        var anchorX = itemRect.left + itemElement.clientWidth;
        if (subMenuElement.clientWidth > window.innerWidth) {
          anchorX = 0;
        } else if (anchorX + subMenuElement.clientWidth > window.innerWidth) {
          anchorX = window.innerWidth - subMenuElement.clientWidth;
        }
        var anchorY = itemRect.top;
        if (subMenuElement.clientHeight > window.innerHeight) {
          anchorY = 0;
        } else if (anchorY + subMenuElement.clientHeight > window.innerHeight) {
          anchorY = window.innerHeight - subMenuElement.clientHeight;
        }
        subMenuElement.style.left = anchorX;
        subMenuElement.style.top = anchorY;
      });

    itemElement.appendChild(subMenuElement);
  }

  if (typeof item.onItemSelected == "function") {
    var listener = function() {
        item.onItemSelected();
        contextMenu.close();
      };
    itemElement.addEventListener("click", listener);
    this.listenerUnregisterCallbacks.push(function() {
      itemElement.removeEventListener("click", listener);
    });
  }
};

/**
 * Closes the context menu.
 */
JSPopupMenu.prototype.close = function() {
  this.getHTMLElement().classList.remove("visible");
  JSPopupMenu.current = null;

  if (this.listenerUnregisterCallbacks) {
    for (var i = 0; i < this.listenerUnregisterCallbacks.length; i++) {
      this.listenerUnregisterCallbacks[i]();
    }
  }

  this.listenerUnregisterCallbacks = null;
  this.getHTMLElement().innerHTML = "";
};

/**
 * Builds items of a context menu which is about to be shown.
 */
JSPopupMenu.Builder = function() {
  /** @type {{ uid?: string, label?: string, iconPath?: string, onItemSelected?: function(), subItems?: {}[] }[] } } */
  this.items = [];
}
JSPopupMenu.Builder.prototype = Object.create(JSPopupMenu.Builder.prototype);
JSPopupMenu.Builder.prototype.constructor = JSPopupMenu.Builder;

/**
 * Add a checkable item
 * @param {string} label
 * @param {function()} [onItemSelected]
 * @param {boolean} [checked]
 */
JSPopupMenu.Builder.prototype.addCheckItem = function(label, onItemSelected, checked) {
  this.addNewItem(label, undefined, onItemSelected, checked === true, "checkbox");
};

/**
 * Add a radio button item
 * @param {string} label
 * @param {function()} [onItemSelected]
 * @param {boolean} [checked]
 */
JSPopupMenu.Builder.prototype.addRadioItem = function(label, onItemSelected, checked) {
  this.addNewItem(label, undefined, onItemSelected, checked === true, "radio");
};

/**
 * Adds an item to this menu using either a ResourceAction, or icon (optional), label & callback.
 * 1) builder.addItem(pane.getAction(MyPane.ActionType.MY_ACTION))
 * 2) builder.addItem('resources/icons/tango/media-skip-forward.png', "myitem", function() { console.log('my item clicked') })
 * 3) builder.addItem("myitem", function() { console.log('my item clicked') })
 *
 * @param {ResourceAction|string} actionOrIconPathOrLabel
 * @param {string|function()} [onItemSelectedCallbackOrLabel]
 * @param {function()} [onItemSelectedCallback]
 *
 * @return {JSPopupMenu.Builder}
 *
 */
JSPopupMenu.Builder.prototype.addItem = function(actionOrIconPathOrLabel, onItemSelectedCallbackOrLabel, onItemSelectedCallback) {
  var label = null;
  var iconPath = null;
  var onItemSelected = null;
  // Defined only for a check action
  var checked = undefined;
  // Defined only for a toggle action
  var selected = undefined;

  if (actionOrIconPathOrLabel instanceof ResourceAction) {
    var action = actionOrIconPathOrLabel;

    // Do no show item if action is disabled
    if (!action.isEnabled() || action.getValue(ResourceAction.VISIBLE) === false) {
      return this;
    }

    label = action.getValue(ResourceAction.POPUP) || action.getValue(AbstractAction.NAME);

    var libIconPath = action.getValue(AbstractAction.SMALL_ICON);
    if (libIconPath != null) {
      iconPath = "lib/" + libIconPath;
    }

    if (action.getValue(ResourceAction.TOGGLE_BUTTON_GROUP)) {
      selected = action.getValue(AbstractAction.SELECTED_KEY);
    }

    onItemSelected = function() {
        action.actionPerformed();
      };
  } else if (typeof onItemSelectedCallback == "function") {
    iconPath = actionOrIconPathOrLabel;
    label = onItemSelectedCallbackOrLabel;
    onItemSelected = onItemSelectedCallback;
  } else {
    label = actionOrIconPathOrLabel;
    onItemSelected = onItemSelectedCallbackOrLabel;
  }

  this.addNewItem(label, iconPath, onItemSelected, selected, selected !== undefined ? "radio" : undefined);

  return this;
}

/**
 * @param {string} label
 * @param {string | undefined} [iconPath]
 * @param {function() | undefined} [onItemSelected]
 * @param {boolean | undefined} [selected]
 * @param {"radio" | "checkbox" | undefined} [mode]
 */
JSPopupMenu.Builder.prototype.addNewItem = function(label, iconPath, onItemSelected, selected, mode) {
  this.items.push({
      uid: UUID.randomUUID(),
      label: label,
      iconPath: iconPath,
      onItemSelected: onItemSelected,
      selected: selected,
      mode: mode
    });
};

/**
 * Adds a sub menu to this menu.
 * @param {ResourceAction|string} action
 * @param {function(JSPopupMenu.Builder)} buildSubMenu
 * @return {JSPopupMenu.Builder}
 */
JSPopupMenu.Builder.prototype.addSubMenu = function(action, buildSubMenu) {
  // Do no show item if action is disabled
  if (action.isEnabled()) {
    var label = action.getValue(ResourceAction.POPUP) || action.getValue(AbstractAction.NAME);
    var libIconPath = action.getValue(AbstractAction.SMALL_ICON);
    var iconPath = null;
    if (libIconPath != null) {
      iconPath = "lib/" + libIconPath;
    }
  
    var subMenuBuilder = new JSPopupMenu.Builder();
    buildSubMenu(subMenuBuilder);
    var subItems = subMenuBuilder.items;
    if (subItems.length > 0) {
      this.items.push({
          uid: UUID.randomUUID(),
          label: label,
          iconPath: iconPath,
          subItems: subItems
        });
    }
  }

  return this;
}

JSPopupMenu.CONTEXT_MENU_SEPARATOR_ITEM = {};

/**
 * Adds a separator after previous items.
 * Does nothing if there are no items yet or if the latest added item is already a separator.
 * @return {JSPopupMenu.Builder}
 */
JSPopupMenu.Builder.prototype.addSeparator = function() {
  if (this.items.length > 0 && this.items[this.items.length - 1] != JSPopupMenu.CONTEXT_MENU_SEPARATOR_ITEM) {
    this.items.push(JSPopupMenu.CONTEXT_MENU_SEPARATOR_ITEM);
  }
  return this;
}

// Global initializations of the toolkit
if (!JSPopupMenu.globalCloserRegistered) {
  document.addEventListener("click", function(ev) {
    if (JSPopupMenu.current != null
      && !JSComponent.isElementContained(ev.target, JSPopupMenu.current.getHTMLElement())) {
      // Clicked outside menu
      if (JSPopupMenu.closeCurrentIfAny()) {
        ev.stopPropagation();
        ev.preventDefault();
      }
    }
  });
  JSPopupMenu.globalCloserRegistered = true;
}

document.addEventListener("keyup", function(ev) {
  if (ev.key == "Escape" || ev.keyCode == 27) {
    JSDialog.closeTopMostDialogIfAny();
    JSPopupMenu.closeCurrentIfAny();
  }
});


/**
 * The root class for component views.
 * @param {UserPreferences} preferences the current user preferences
 * @param {HTMLElement} spanElement span element on which the spinner is installed
 * @param {{format?: Format, nullable?: boolean, value?: number, minimum?: number, maximum?: number, stepSize?: number}} [options]
 * - format: number format to be used for this input - default to DecimalFormat for current content
 * - nullable: false if null/undefined is not allowed - default true
 * - value: initial value,
 * - min: minimum number value,
 * - max: maximum number value,
 * - stepSize: step between values when increment / decrement using UI - default 1
 * @constructor
 * @extends JSComponent
 * @author Louis Grignon
 */
function JSSpinner(preferences, spanElement, options) {
  if (spanElement.tagName.toUpperCase() != "SPAN") {
    throw new Error("JSSpinner: please provide a span for the spinner to work - " + spanElement + " is not a span");
  }

  if (!options) { 
    options = {}; 
  }
  this.checkMinimumMaximum(options.minimum, options.maximum);

  if (!isNaN(parseFloat(options.minimum))) { 
    this.minimum = options.minimum;
  }
  if (!isNaN(parseFloat(options.maximum))) { 
    this.maximum = options.maximum;
  }
  if (isNaN(parseFloat(options.stepSize))) { 
    this.stepSize = 1; 
  } else {
    this.stepSize = options.stepSize;
  }
  if (typeof options.nullable == "boolean") { 
    this.nullable = options.nullable;
  } else {
    this.nullable = false; 
  }
  if (options.format instanceof Format) { 
    this.format = options.format;
  } else {
    this.format = new DecimalFormat(); 
  }

  var component = this;
  JSComponent.call(this, preferences, spanElement, true);

  spanElement.classList.add("spinner");
  
  this.textInput = document.createElement("input");
  this.textInput.type = "text";
  spanElement.appendChild(this.textInput);

  this.registerEventListener(this.textInput, "focus", function(ev) {
      component.updateUI();
    });
  this.registerEventListener(this.textInput, "focusout", function(ev) {
      component.updateUI();
    });

  this.registerEventListener(this.textInput, "input", function(ev) {
      if (component.isFocused()) {
        var pos = new ParsePosition(0);
        var inputValue = component.parseValueFromInput(pos);
        if (pos.getIndex() != component.textInput.value.length
            || inputValue == null && !component.nullable
            || (component.minimum != null && inputValue < component.minimum) 
            || (component.maximum != null && inputValue > component.maximum)) {
          component.textInput.style.color = "red";
        } else {
          component.textInput.style.color = null;
          component.value = inputValue;
        }
      }
    });

  this.registerEventListener(this.textInput, "blur", function(ev) {
      var inputValue = component.parseValueFromInput();
      if (inputValue == null && !component.nullable) {
        var restoredValue = component.value;
        if (restoredValue == null) {
          restoredValue = component.getDefaultValue();
        }
        inputValue = restoredValue;
      }
      component.textInput.style.color = null;
      component.setValue(inputValue);
    });

  this.initIncrementDecrementButtons(spanElement);

  Object.defineProperty(this, "width", {
      get: function() { return spanElement.style.width; },
      set: function(value) { spanElement.style.width = value; }
    });
  Object.defineProperty(this, "parentElement", {
      get: function() { return spanElement.parentElement; }
    });
  Object.defineProperty(this, "previousElementSibling", {
      get: function() { return spanElement.previousElementSibling; }
    });
  Object.defineProperty(this, "style", {
      get: function() { return spanElement.style; }
    });

  this.setValue(options.value);
}
JSSpinner.prototype = Object.create(JSComponent.prototype);
JSSpinner.prototype.constructor = JSSpinner;

/**
 * @return {Object} the value of this spinner
 */
JSSpinner.prototype.getValue = function() {
  return this.value;
}

/**
 * @param {Object} value the value of this spinner
 */
JSSpinner.prototype.setValue = function(value) {
  if (value instanceof Big) {
    value = parseFloat(value);
  }
  if (value != null && typeof value != "number") {
    throw new Error("JSSpinner: Expected values of type number");
  }
  if (value == null && !this.nullable) {
    value = this.getDefaultValue();
  }
  if (value != null && this.minimum != null && value < this.minimum) {
    value = this.minimum;
  }
  if (value != null && this.maximum != null && value > this.maximum) {
    value = this.maximum;
  }

  if (value != this.value) {
    this.value = value;
    this.updateUI();
  }
}

/**
 * @return {number} minimum of this spinner
 * @private
 */
JSSpinner.prototype.checkMinimumMaximum = function(minimum, maximum) {
  if (minimum != null && maximum != null && minimum > maximum) {
    throw new Error("JSSpinner: minimum is not below maximum - minimum = " + minimum + " maximum = " + maximum);
  }
}

/**
 * @return {boolean} <code>true</code> if this spinner may contain no value
 */
JSSpinner.prototype.isNullable = function() {
  return this.nullable;
}

/**
 * @param {boolean} nullable <code>true</code> if this spinner may contain no value
 */
JSSpinner.prototype.setNullable = function(nullable) {
  var containsNullValue = this.nullable && this.value === null;
  this.nullable = nullable;
  if (!nullable && containsNullValue) {
    this.value = this.getDefaultValue();
  }
}

/**
 * @return {Format} format used to format the value of this spinner
 */
JSSpinner.prototype.getFormat = function() {
  return this.format;
}

/**
 * @param {Format} format  format used to format the value of this spinner
 */
JSSpinner.prototype.setFormat = function(format) {
  this.format = format;
  this.updateUI();
}

/**
 * @return {number} minimum of this spinner
 */
JSSpinner.prototype.getMinimum = function() {
  return this.minimum;
}

/**
 * @param {number} minimum minimum value of this spinner
 */
JSSpinner.prototype.setMinimum = function(minimum) {
  this.checkMinimumMaximum(minimum, this.maximum);
  this.minimum = minimum; 
}

/**
 * @return {number} minimum of this spinner
 */
JSSpinner.prototype.getMinimum = function() {
  return this.minimum;
}

/**
 * @param {number} minimum minimum value of this spinner
 */
JSSpinner.prototype.setMinimum = function(minimum) {
  this.checkMinimumMaximum(minimum, this.maximum);
  this.minimum = minimum; 
}

/**
 * @return {number} maximum of this spinner
 */
JSSpinner.prototype.getMaximum = function() {
  return this.maximum;
}

/**
 * @param {number} maximum maximum value of this spinner
 */
JSSpinner.prototype.setMaximum = function(maximum) {
  this.checkMinimumMaximum(this.minimum, maximum);
  this.maximum = maximum; 
}

/**
 * @return {number} step size of this spinner
 */
JSSpinner.prototype.getStepSize = function() {
  return this.stepSize;
}

/**
 * @param {number} stepSize step size of this spinner
 */
JSSpinner.prototype.setStepSize = function(stepSize) {
  this.stepSize = stepSize; 
}

/**
 * @return {HTMLInputElement} underlying input element
 */
JSSpinner.prototype.getInputElement = function() {
  return this.textInput;
}

JSSpinner.prototype.addEventListener = function() {
  return this.textInput.addEventListener.apply(this.textInput, arguments);
}

JSSpinner.prototype.removeEventListener = function() {
  return this.textInput.removeEventListener.apply(this.textInput, arguments);
}

/**
 * Refreshes UI for current state / options. For instance, if format has changed, displayed text is updated.
 * @private
 */
JSSpinner.prototype.updateUI = function() {
  this.textInput.value = this.formatValueForUI(this.value);
}

/**
 * @param {ParsePosition} [parsePosition]
 * @return {number}
 * @private
 */
JSSpinner.prototype.parseValueFromInput = function(parsePosition) {
  if (!this.textInput.value || this.textInput.value.trim() == "") {
    if (this.nullable) {
      return null;
    } else {
      return this.value;
    }
  }
  return this.format.parse(this.textInput.value, 
      parsePosition != undefined ? parsePosition : new ParsePosition(0));
}

/**
 * @return {number}
 * @private
 */
JSSpinner.prototype.getDefaultValue = function() {
  var defaultValue = 0;
  if (this.minimum != null && this.minimum > defaultValue) {
    defaultValue = this.minimum;
  }
  if (this.maximum != null && this.maximum < defaultValue) {
    defaultValue = this.maximum;
  }
  return defaultValue;
}

/**
 * @param {number} value
 * @return {string}
 * @private
 */
JSSpinner.prototype.formatValueForUI = function(value) {
  if (value == null) {
    return "";
  }

  if (!this.isFocused()) {
    return this.format.format(value);
  }
  if (this.noGroupingFormat == null || this.lastFormat !== this.format) {
    // Format changed, compute focused format
    this.lastFormat = this.format;
    this.noGroupingFormat = this.lastFormat.clone();
    this.noGroupingFormat.setGroupingUsed(false);
  }
  return this.noGroupingFormat.format(value);
}

/**
 * @return {boolean} true if this spinner has focus
 * @private
 */
JSSpinner.prototype.isFocused = function() {
  return this.textInput === document.activeElement;
}

/**
 * Creates and initialize increment & decrement buttons + related keystrokes.
 * @private
 */
JSSpinner.prototype.initIncrementDecrementButtons = function(spanElement) {
  var component = this;
  this.incrementButton = document.createElement("button");
  this.incrementButton.setAttribute("increment", "");
  this.incrementButton.textContent = "+";
  this.incrementButton.tabIndex = -1;
  spanElement.appendChild(this.incrementButton);

  this.decrementButton = document.createElement("button");
  this.decrementButton.setAttribute("decrement", "");
  this.decrementButton.textContent = "-";
  this.decrementButton.tabIndex = -1;
  spanElement.appendChild(this.decrementButton);

  var incrementValue = function(ev) {
      var previousValue = component.value;
      if (previousValue == null || isNaN(previousValue)) {
        previousValue = component.getDefaultValue();
      }
      component.setValue(previousValue + component.stepSize);
      component.fireInputEvent();
    };
  var decrementValue = function(ev) {
      var previousValue = component.value;
      if (previousValue == null || isNaN(previousValue)) {
        previousValue = component.getDefaultValue();
      }
      component.setValue(previousValue - component.stepSize);
      component.fireInputEvent();
    };

  // Repeat incrementValue / decrementValue every 80 ms with an initial delay of 400 ms
  // while mouse button kept pressed, and ensure at least one change is triggered for a short click
  var repeatAction = function(button, action) {
      var stopRepeatedTask = function(ev) {
          clearTimeout(taskId);
          button.removeEventListener("mouseleave", stopRepeatedTask);
          button.removeEventListener("mouseup", stopRepeatedTask);
        };
      var clickAction = function(ev) {
          clearTimeout(taskId);
          button.removeEventListener("click", clickAction);
          action();
        };
      button.addEventListener("click", clickAction);
      var repeatedTask = function() {
          action();
          taskId = setTimeout(repeatedTask, 80); 
        };
      var taskId = setTimeout(function() {
          button.removeEventListener("click", clickAction);
          button.addEventListener("mouseleave", stopRepeatedTask);
          button.addEventListener("mouseup", stopRepeatedTask);
          repeatedTask();
        }, 400);
    };
  var repeatIncrementValue = function(ev) {
      repeatAction(component.incrementButton, incrementValue);
    };
  this.registerEventListener(component.incrementButton, "mousedown", repeatIncrementValue);
  
  var repeatDecrementValue = function(ev) {
      repeatAction(component.decrementButton, decrementValue);
    };
  this.registerEventListener(component.decrementButton, "mousedown", repeatDecrementValue);

  this.registerEventListener(component.textInput, "keydown", function(ev) {
      var keyStroke = KeyStroke.getKeyStrokeForEvent(ev, "keydown");
      if (keyStroke.lastIndexOf(" UP") > 0) {
        ev.stopImmediatePropagation();
        incrementValue();
      } else if (keyStroke.lastIndexOf(" DOWN") > 0) {
        ev.stopImmediatePropagation();
        decrementValue();
      }
    });
}

/**
 * Fires an "input" event on behalf of underlying text input.
 * @private
 */
JSSpinner.prototype.fireInputEvent = function() {
  var ev = document.createEvent("Event");
  ev.initEvent("input", true, true);
  this.textInput.dispatchEvent(ev);
}

/**
 * Enables or disables this component.
 * @param {boolean} enabled
 */
JSSpinner.prototype.setEnabled = function(enabled) {
  this.textInput.disabled = !enabled;
  this.incrementButton.disabled = !enabled;
  this.decrementButton.disabled = !enabled;
}


/**
 * A combo box component which allows any type of content (e.g. images).
 * @param {UserPreferences} preferences the current user preferences
 * @param {HTMLElement} selectElement HTML element on which install this component
 * @param {{nullable?: boolean, value?: any, availableValues: (any)[], renderCell?: function(value: any, element: HTMLElement), selectionChanged: function(newValue: any)}} [options]
 * - nullable: false if null/undefined is not allowed - default true
 * - value: initial value - default undefined if nullable or first available value,
 * - availableValues: available values in this combo,
 * - renderCell: a function which builds displayed element for a given value - defaults to setting textContent to value.toString()
 * - selectionChanged: called with new value when selected by user
 * @constructor
 * @extends JSComponent
 * @author Louis Grignon
 */
function JSComboBox(preferences, selectElement, options) {
  if (!options) { 
    options = {}; 
  }
  if (typeof options.nullable != "boolean") { 
    options.nullable = false; 
  }
  if (!Array.isArray(options.availableValues) || options.availableValues.length <= 0) {
    throw new Error("JSComboBox: No available values provided");
  }
  if (typeof options.renderCell != "function") {
    options.renderCell = function(value, element) {
        element.textContent = value == null ? "" : value.toString();
      };
  }
  if (options.value == null && !options.nullable) {
    options.value = options.availableValues[0];
  }

  var component = this;
  JSComponent.call(this, preferences, selectElement, true);

  this.options = options;

  selectElement.classList.add("combo-box");

  this.button = document.createElement("button");
  selectElement.appendChild(this.button);

  this.preview = document.createElement("div");
  this.preview.classList.add("preview");
  this.button.appendChild(this.preview);

  this.initSelectionPanel();

  this.registerEventListener(this.button, "click", function(ev) {
      ev.stopImmediatePropagation();
      component.openSelectionPanel(ev.pageX, ev.pageY);
    });

  this.setSelectedItem(options.value);
}
JSComboBox.prototype = Object.create(JSComponent.prototype);
JSComboBox.prototype.constructor = JSComboBox;

/**
 * @private
 */
JSComboBox.prototype.initSelectionPanel = function() {
  var selectionPanel = document.createElement("div");
  selectionPanel.classList.add("selection-panel");

  for (var i = 0; i < this.options.availableValues.length; i++) {
    var currentItemElement = document.createElement("div");
    currentItemElement.value = this.options.availableValues[i];
    this.options.renderCell(currentItemElement.value, currentItemElement);
    selectionPanel.appendChild(currentItemElement);
  }

  this.getHTMLElement().appendChild(selectionPanel);
  this.selectionPanel = selectionPanel;

  var component = this;
  this.registerEventListener(selectionPanel.children, "click", function(ev) {
      component.selectedItem = this.value;
      component.updateUI();
      if (typeof component.options.selectionChanged == "function") {
        component.options.selectionChanged(component.selectedItem);
      }
    });
}

/**
 * @return {number} the value selected in this combo box
 */
JSComboBox.prototype.getSelectedItem = function() {
  return this.selectedItem;
}

/**
 * @param {number} selectedItem  the value to select in this combo box
 */
JSComboBox.prototype.setSelectedItem = function(selectedItem) {
  var isValueAvailable = false;
  for (var i = 0; i < this.options.availableValues.length; i++) {
    if (this.areValuesEqual(selectedItem, this.options.availableValues[i])) {
      isValueAvailable = true;
      break;
    }
  }
  if (!isValueAvailable) {
    selectedItem = null;
  }

  if (selectedItem == null && !this.options.nullable) {
    selectedItem = this.options.availableValues[0];
  }

  if (!this.areValuesEqual(selectedItem, this.selectedItem)) {
    this.selectedItem = selectedItem;
    this.updateUI();
  }
}

/**
 * Enables or disables this combo box.
 * @param {boolean} enabled 
 */
JSComboBox.prototype.setEnabled = function(enabled) {
  this.button.disabled = !enabled;
}

/**
 * Opens the combo box's selectionPanel
 * @param {number} pageX
 * @param {number} pageY
 *
 * @private
 */
JSComboBox.prototype.openSelectionPanel = function(pageX, pageY) {
  var selectionPanel = this.selectionPanel;
  var closeSelectorPanel = function() {
      document.removeEventListener("click", closeSelectorPanel);
      selectionPanel.style.opacity = 0;
      selectionPanel.style.display = "none";
    }

  selectionPanel.style.display = "block";
  selectionPanel.style.opacity = 1;
  selectionPanel.style.left = pageX + selectionPanel.clientWidth > window.width ? window.width - selectionPanel.clientWidth : pageX;
  selectionPanel.style.top = pageY + selectionPanel.clientHeight > window.innerHeight ? window.innerHeight - selectionPanel.clientHeight : pageY;
  document.addEventListener("click", closeSelectorPanel);
}

/**
 * Refreshes UI, i.e. preview of selected value.
 */
JSComboBox.prototype.updateUI = function() {
  this.preview.innerHTML = "";
  this.options.renderCell(this.getSelectedItem(), this.preview);
}

/**
 * Checks if value1 and value2 are equal. Returns true if so.
 * NOTE: this internally uses JSON.stringify to compare values
 * @return {boolean}
 * @private
 */
JSComboBox.prototype.areValuesEqual = function(value1, value2) {
  return JSON.stringify(value1) == JSON.stringify(value2);
}


/*
 * @typedef {{
 *   visibleColumnNames?: string[],
 *   expandedRowsIndices?: number[],
 *   expandedRowsValues?: any[],
 *   sort?: { columnName: string, direction: "asc" | "desc" }
 * }} TreeTableState
 * @property TreeTableState.expandedRowsIndices index in filtered and sorted rows, expandedRowsValues can also be used but not both (expandedRowsValues will be preferred)
 * @property TreeTableState.expandedRowsValues expanded rows listed by their values. It takes precedence over expandedRowsIndices but achieves the same goal
 */
/*
 * @typedef {{
 *   columns: {
 *       name: string,
 *       orderIndex: number,
 *       label: string,
 *       defaultWidth?: string
 *   }[],
 *   renderCell: function(value: any, columnName: string, cell: HTMLElement): void,
 *   getValueComparator: function(sortConfig?: { columnName: string, direction: "asc" | "desc" }): function(value1: any, value2: any),
 *   selectionChanged: function(values: any[]): void,
 *   rowDoubleClicked: function(value: any): void,
 *   expandedRowsChanged: function(expandedRowsValues: any[], expandedRowsIndices: number[]): void,
 *   sortChanged: function(sort: { columnName: string, direction: "asc" | "desc" }): void,
 *   initialState?: TreeTableState
 * }} TreeTableModel
 * @property TreeTableModel.renderCell render cell to given html element for given value, column name
 * @property TreeTableModel.selectionChanged called when a row selection changes, passing updated selected values
 * @property TreeTableModel.rowDoubleClicked called when a row is double clicked, passing row's value
 */

/**
 * A flexible tree table which allows grouping (tree aspect), sorting, some inline edition, single/multi selection, contextual menu, copy/paste.
 * @param {HTMLElement} container html element on which this component is installed
 * @param {UserPreferences} preferences the current user preferences
 * @param {TreeTableModel} model table's configuration
 * @param {{value: any, children: {value, children}[] }[]} [data] data source for this tree table - defaults to empty data
 * @constructor
 * @extends JSComponent
 * @author Louis Grignon
 */
// TODO LOUIS contextual menu
function JSTreeTable(container, preferences, model, data) {
  JSComponent.call(this, preferences, container, true);
  
  /**
   * @type {TreeTableState}
   */
  this.state = {};
  this.selectedRowsValues = [];
  
  this.tableElement = document.createElement("div");
  this.tableElement.classList.add("tree-table");
  container.appendChild(this.tableElement);
  this.setModel(model);
  this.setData(data ? data : []);
}
JSTreeTable.prototype = Object.create(JSComponent.prototype);
JSTreeTable.prototype.constructor = JSTreeTable;

/**
 * Sets data and updatees rows in UI.
 * @param {{value: any, children: {value, children}[] }[]} data
 */
JSTreeTable.prototype.setData = function(data) {
  this.data = data;

  var expandedRowsValues = this.getExpandedRowsValues();
  if (expandedRowsValues != null) {
    this.updateState({
        expandedRowsValues: expandedRowsValues
      });
  }
  this.generateTableRows();

  this.fireExpandedRowsChanged();
}

/**
 * Gets current table data
 * @return {{value: any, children: {value, children}[] }[]}
 */
JSTreeTable.prototype.getData = function() {
  return this.data;
}

/**
 * @param {TreeTableModel} model
 */
JSTreeTable.prototype.setModel = function(model) {
  this.model = model;

  this.updateState(model.initialState);
  this.columnsWidths = this.getColumnsWidthByName();

  this.generateTableHeaders();
  this.generateTableRows();
}

/**
 * @param {any[]} values
 */
JSTreeTable.prototype.setSelectedRowsByValue = function(values) {
  this.selectedRowsValues = values;
  this.generateTableRows();
  this.expandSelectedRows();
  this.scrollToSelectedRowsIfNotVisible();
}

/**
 * @return {HTMLElement[]}
 * @private
 */
JSTreeTable.prototype.getSelectedRows = function() {
  return this.bodyElement.querySelectorAll(".selected");
}

/**
 * @private
 */
JSTreeTable.prototype.expandSelectedRows = function() {
  var selectedRows = this.getSelectedRows();
  for (var i = 0; i < selectedRows.length; i++) {
    if (selectedRows[i]._model.parentGroup) {
      this.expandOrCollapseRow(selectedRows[i]._model.parentGroup, true);
    }
  }
}

/**
 * @private
 */
JSTreeTable.prototype.scrollToSelectedRowsIfNotVisible = function() {
  var body = this.bodyElement;
  var selectedRows = this.getSelectedRows();

  if (selectedRows.length > 0) {
    // If one selected row is visible, do not change scroll
    for (var i = 0; i < selectedRows.length; i++) {
      var selectedRow = selectedRows[i];
      var rowYTop = selectedRow.offsetTop - body.offsetTop;
      var rowYBottom = rowYTop + selectedRow.height;
      if (rowYTop >= body.scrollTop && rowYTop <= (body.scrollTop + body.clientHeight)
        || rowYBottom >= body.scrollTop && rowYBottom <= (body.scrollTop + body.clientHeight)) {
        return;
      }
    }

    body.scrollTop = selectedRows[0].offsetTop - body.offsetTop;
  }
}

/**
 * @return {any[]} expanded rows by their values
 * @private
 */
JSTreeTable.prototype.getExpandedRowsValues = function() {
  if (this.state && this.state.expandedRowsValues) {
    return this.state.expandedRowsValues;
  }
  return undefined;
}

/**
 * @private
 */
JSTreeTable.prototype.fireExpandedRowsChanged = function() {
  if (this.state.expandedRowsValues != null) {
    this.updateExpandedRowsIndices();
    this.model.expandedRowsChanged(this.state.expandedRowsValues, this.state.expandedRowsIndices);
  }
}

/**
 * Refreshes expandedRowsIndices from expandedRowsValues
 * @private
 */
JSTreeTable.prototype.updateExpandedRowsIndices = function() {
  if (this.state.expandedRowsValues != null 
      && this.data != null 
      && this.data.sortedList != null) {
    this.state.expandedRowsIndices = [];
    for (var i = 0; i < this.data.sortedList.length; i++) {
      var value = this.data.sortedList[i].value;
      if (this.state.expandedRowsValues.indexOf(value) > -1) {
        this.state.expandedRowsIndices.push(i);
      }
    }
  }
}

/**
 * @private
 */
JSTreeTable.prototype.fireSortChanged = function() {
  if (this.state.sort != null) {
    this.model.sortChanged(this.state.sort);
  }
}

/**
 * @param {Partial<TreeTableState>} [stateProperties]
 * @private
 */
JSTreeTable.prototype.updateState = function(stateProperties) {
  if (stateProperties) {
    CoreTools.merge(this.state, stateProperties);
  }
}

/**
 * @return {function(value1: any, value2: any)}
 * @private
 */
JSTreeTable.prototype.getValueComparator = function() {
  return this.model.getValueComparator(this.state.sort);
}

/**
 * @private
 */
JSTreeTable.prototype.generateTableHeaders = function() {
  var treeTable = this;

  var head = this.tableElement.querySelector("[header]");
  if (!head) {
    head = document.createElement("div");
    head.setAttribute("header", "true");
    this.tableElement.appendChild(head);
    this.tableElement.appendChild(document.createElement("br"));
  }
  head.innerHTML = "";

  var columns = this.getColumns();
  for (var i = 0; i < columns.length; i++) {
    var column = columns[i];
    var headCell = document.createElement("div");
    head.appendChild(headCell);
    headCell.setAttribute("cell", "true");
    headCell.textContent = column.label;
    headCell.dataset["name"] = column.name;
    if (this.state.sort && this.state.sort.columnName == column.name) {
      headCell.classList.add("sort");
      if (this.state.sort.direction == "desc") {
        headCell.classList.add("descending");
      }
    }

    headCell.style.width = treeTable.getColumnWidth(column.name);
  }
  this.registerEventListener(head.children, "click", function(ev) {
      var columnName = this.dataset["name"];
      var descending = this.classList.contains("sort") && !this.classList.contains("descending");
      treeTable.sortTable(columnName, descending);
    });
}

/**
 * @private
 */
JSTreeTable.prototype.generateTableRows = function() {
  if (!this.data) {
    return;
  }

  var treeTable = this;

  var scrollTop = 0;
  var body = this.bodyElement;
  if (body) {
    scrollTop = body.scrollTop;
    body.parentElement.removeChild(body);
  }
  var body = this.bodyElement = document.createElement("div");
  body.setAttribute("body", "true");

  var columns = this.getColumns();
  var columnNames = [];
  for (var i = 0; i < columns.length; i++) {
    columnNames.push(columns[i].name);
  }

  var comparator = this.getValueComparator();

  // Generate simplified table model: a sorted list of items
  var sortedList = this.data.sortedList = [];

  /**
   * @param {{value: any, children: any[]}[]} currentNodes
   * @param {number} currentIndentation
   * @return {Object[]} generated children items
   */
  var sortDataTree = function(currentNodes, currentIndentation, parentGroup) {
      // Children nodes are hidden by default, and will be flagged as visible with setCollapsed, see below
      var hideChildren = currentIndentation > 0;
  
      var sortedCurrentNodes = comparator != null
          ? currentNodes.sort(function(leftNode, rightNode) {
                return comparator(leftNode.value, rightNode.value);
              })
          : currentNodes;
      var currentNodesItems = [];
      for (var i = 0; i < sortedCurrentNodes.length; i++) {
        var currentNode = sortedCurrentNodes[i];
        var currentNodeSelected = treeTable.selectedRowsValues.indexOf(currentNode.value) > -1;
        var selected = (parentGroup && parentGroup.selected) || currentNodeSelected;
        var sortedListItem = {
          value: currentNode.value,
          indentation: currentIndentation,
          group: false,
          parentGroup: parentGroup,
          selected: selected,
          hidden: hideChildren,
          collapsed: undefined,
          childrenItems: undefined,
          setCollapsed: function() {},
          isInCollapsedGroup: function() {
            var parent = this;
            while ((parent = parent.parentGroup)) {
              if (parent.collapsed === true) {
                return true;
              }
            }
            return false;
          }
        };
        currentNodesItems.push(sortedListItem);
        sortedList.push(sortedListItem);
  
        // Create node's children items
        if (Array.isArray(currentNode.children) && currentNode.children.length > 0) {
          sortedListItem.group = true;
          sortedListItem.collapsed = true;
          sortedListItem.childrenItems = sortDataTree(currentNode.children, currentIndentation + 1, sortedListItem);
          sortedListItem.setCollapsed = (function(item) {
              return function(collapsed) {
                item.collapsed = collapsed;
                for (var i = 0; i < item.childrenItems.length; i++) {
                  item.childrenItems[i].hidden = collapsed;
                }
              }
            })(sortedListItem);
        }
      }
  
      return currentNodesItems;
    };
  sortDataTree(this.data.slice(0), 0);

  // Synchronize expandedRowsIndices/expandedRowsValues & flag groups as expanded, and children as visible
  this.updateExpandedRowsIndices();
  if (this.state.expandedRowsIndices && this.state.expandedRowsIndices.length > 0) {
    var expandedRowsValues = [];
    for (var i = 0; i < this.state.expandedRowsIndices.length; i++) {
      var item = sortedList[this.state.expandedRowsIndices[i]];
      if (item) {
        expandedRowsValues.push(item.value);
        if (!item.isInCollapsedGroup()) {
          item.setCollapsed(false);
        }
      }
    }
    if (expandedRowsValues.length > 0) {
      this.state.expandedRowsValues = expandedRowsValues;
    }
  }

  // Generate DOM for items
  for (var i = 0; i < sortedList.length; i++) {
    var row = this.generateRowElement(columnNames, i, sortedList[i]);
    body.appendChild(row);
  }

  this.tableElement.appendChild(body);

  body.scrollTop = scrollTop;
};

/**
 * @param {string[]} columnNames
 * @param {number} rowIndex
 * @param {{
        value: any,
        indentation: number,
        group: boolean,
        selected: boolean,
        hidden: boolean,
        collapsed?: boolean,
        childrenItems?: boolean,
        setCollapsed: function(),
    }} rowModel
 * @private
 */
JSTreeTable.prototype.generateRowElement = function(columnNames, rowIndex, rowModel) {
  var treeTable = this;
  var row = document.createElement("div");
  row.setAttribute("row", "true");

  var mainCell = null;
  for (var j = 0; j < columnNames.length; j++) {
    var columnName = columnNames[j];
    var cell = document.createElement("div");
    cell.setAttribute("cell", "true");
    treeTable.model.renderCell(rowModel.value, columnName, cell);
    cell.style.width = treeTable.getColumnWidth(columnName);

    if (mainCell == null || cell.classList.contains("main")) {
      mainCell = cell;
    }

    row.appendChild(cell);
  }

  if (mainCell != null) {
    mainCell.classList.add("main");
    mainCell.style.paddingLeft = (15 + rowModel.indentation * 10) + "px";
    if (rowModel.group) {
      treeTable.registerEventListener(mainCell, "click", function(ev) {
          ev.stopImmediatePropagation();
          treeTable.expandOrCollapseRow(rowModel, mainCell.parentElement.classList.contains("collapsed"));
          return false;
        });

      row.classList.add("group");
      if (rowModel.collapsed) {
        row.classList.add("collapsed");
      }
    }
  }
  if (rowModel.hidden) {
    row.style.display = "none";
  }
  if (rowModel.selected) {
    row.classList.add("selected");
  }
  row._model = rowModel;

  treeTable.registerEventListener(row, "click", function(ev) {
      var row = this;
      var rowValue = row._model.value;
  
      row.classList.add("selected");
      var child = row;
      while ((child = child.nextSibling) && child._model.indentation > row._model.indentation) {
        child.classList.add("selected");
      }
  
      if (ev.shiftKey) {
        treeTable.selectedRowsValues.push(rowValue);
      } else {
        treeTable.selectedRowsValues = [rowValue];
      }
      if (typeof treeTable.model.selectionChanged == "function") {
        treeTable.model.selectionChanged(treeTable.selectedRowsValues);
      }
    });
  treeTable.registerEventListener(row, "dblclick", function(ev) {
      if (typeof treeTable.model.rowDoubleClicked == "function") {
        var row = this;
        var rowValue = row._model.value;
        treeTable.model.rowDoubleClicked(rowValue);
      }
    });

  return row;
}

/**
 * @param {Object} rowModel
 * @param {boolean} expand true if expanded, false if collapsed
 * @private
 */
JSTreeTable.prototype.expandOrCollapseRow = function(rowModel, expand) {
  var treeTable = this;

  // TODO LOUIS test on touch device
  if (treeTable.state.expandedRowsValues == null) {
    treeTable.state.expandedRowsValues = [];
  }
  if (expand) {
    treeTable.state.expandedRowsValues.push(rowModel.value);
  } else {
    var index = treeTable.state.expandedRowsValues.indexOf(rowModel.value);
    if (index > -1) {
      treeTable.state.expandedRowsValues.splice(index, 1);
    }
  }
  this.generateTableRows();
  this.fireExpandedRowsChanged();
}

/**
 * @param {string} columnName
 * @param {boolean} descending
 * @private
 */
JSTreeTable.prototype.sortTable = function(columnName, descending) {
  if (!this.state.sort) {
    this.state.sort = {};
  }
  this.state.sort.columnName = columnName;
  this.state.sort.direction = descending ? "desc" : "asc";

  this.fireSortChanged(this.state.sort);
}

/**
 * @param {string} columnName
 * @return {string} css width value, e.g. "2em"
 * @private
 */
JSTreeTable.prototype.getColumnWidth = function(columnName) {
  return this.columnsWidths[columnName];
}

/**
 * @private
 */
JSTreeTable.prototype.getColumns = function() {
  return this.model.columns.slice(0);
}

/**
 * @return {{[name: string]: string}}
 * @see getColumnWidth(name)
 * @private
 */
JSTreeTable.prototype.getColumnsWidthByName = function() {
  var columns = this.model.columns;
  var widths = {};
  for (var i = 0; i < columns.length; i++) {
    var column = columns[i];
    var width = column.defaultWidth ? column.defaultWidth : "6rem";
    widths[column.name] = width;
  }
  return widths;
}