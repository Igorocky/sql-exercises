function focusOnLoad(selector) {
    $(function() {
        $(selector).focus();
    });
}

function doPost(params) {
    $.ajax({
        type: "POST",
        url: params.url,
        data: JSON.stringify(params.data),
        contentType: "application/json; charset=utf-8",
        success: params.success
    });
}

function doGet(params) {
    $.ajax({
        type: "GET",
        url: params.url,
        success: params.success
    });
}

function cleanSelectionCheckboxes() {
    $(".selection-checkbox").each(function (idx) {
        $(this).prop("checked", false);
    })
}

function submitSelection(actionType, selectedElems, onSuccess) {
    let selection = {
        actionType: actionType,
        selections: _.map(selectedElems, selectedElemToSelectionPart())
    };
    doPost({
        url: "/select",
        data: selection,
        success: onSuccess
    });
}

function selectedElemToSelectionPart() {
    return function (selectedElem) {
        return {
            objectType: $(selectedElem).attr("objecttype"),
            selectedId: $(selectedElem).attr("id")
        }
    }
}

function selectionModeOn() {
    cleanSelectionCheckboxes();
    $( ".selection-checkbox" ).show();
    $( "#selection-mode-on-btn" ).hide();
    $( "#selection-mode-off-btn" ).show();
    $( "#cut-btn" ).show();
    $( ".content-wrapper" ).css({
        "border-color": "blue",
        "border-width":"1px",
        "border-style":"solid"
    });
}

function selectionModeOff() {
    cleanSelectionCheckboxes();
    $( ".selection-checkbox" ).hide();
    $( "#selection-mode-on-btn" ).show();
    $( "#selection-mode-off-btn" ).hide();
    $( "#cut-btn" ).hide();
    $( ".content-wrapper" ).css({
        "border-style":"none"
    });
}

function doSelection(actionType) {
    let selectedElems = _.filter($(".selection-checkbox").toArray(), function (elem) {return elem.checked});
    submitSelection(
        actionType,
        selectedElems,
        function () {
            _.each(selectedElems, function (elem) {elem.checked = false});
            selectionModeOff();
        }
    )
}
function doPaste(destId) {
    doPost({
        url: "/performActionOnSelectedObjects",
        data: destId == "null" ? null : destId,
        success: function () {
            window.location.reload();
        }
    });
}

function indexOf(list, predicate) {
    return _.reduce(
        list,
        function (memo, elem) {
            if (predicate(elem)) {
                return {i:memo.i+1, r: memo.i};
            } else {
                return {i:memo.i+1, r: memo.r};
            }
        },
        {i:0, r: null}
    ).r;
}

function extractFileFromEvent(event) {
    // use event.originalEvent.clipboard for newer chrome versions
    var items = (event.clipboardData  || event.originalEvent.clipboardData).items;
    // console.log(JSON.stringify(items)); // will give you the mime types
    // find pasted image among pasted items
    var blob = null;
    for (var i = 0; i < items.length; i++) {
        if (items[i].type.indexOf("image") === 0) {
            blob = items[i].getAsFile();
        }
    }
    return blob;
}

//params(imageType, file, onSuccess)
function uploadImage(params) {
    let fd = new FormData();
    fd.append("imageType", params.imageType);
    if (params.file) {
        fd.append("file", params.file);
    }
    $.ajax({
        type: "POST",
        url: params.file ? "/uploadImage" : "uploadImageFromServ",
        data: fd,
        contentType: false,
        cache: false,
        dataType: 'json',
        processData: false,
        success: function (data) {
            params.onSuccess(data);
        }
    });
}

function registerShortcuts() {
  registerShortcutsOnElems("a", function (elem) {elem.click();});
  registerShortcutsOnElems("button", function (elem) {elem.click();});
  registerShortcutsOnElems("div, ul", focusFirstChild("a"));
}

function registerShortcutsOnElems(elemsSelector, action) {
    $(elemsSelector).each(function (idx, elem) {
        if (elem.hasAttribute("shortcut")) {
            let shortcut = elem.getAttribute("shortcut");
            registerShortcut(createEventSelector(shortcut), function () {action(elem);});
            $(elem).attr("title", shortcut);
        }
    })
}

function focusFirstChild(childSelector) {
    return function (elem) {
        $(elem).find(childSelector + ":first").focus();
    }
}

function registerShortcut(eventSelector, action) {
    document.addEventListener(
        "keyup",
        function onKeyUp(event) {
            if (eventSelector(event)) {
                action();
            }
        },
        false
    );
}

function createEventSelector(shortcutStr) {
    let arr = shortcutStr.split(" ");
    if (_.size(arr) == 1) {
        return function(event) {
            return event.key == shortcutStr;
        }
    } else if (_.size(arr) == 2) {
        if (_.first(arr) == "ctrl") {
            return function(event) {
                return event.ctrlKey && event.key == _.last(arr);
            }
        }
    }
}

function confirmCancelDialog(dialogContainerId, question, confirmButtonName, onConfirm) {
    $('#' + dialogContainerId).html(
        $("<p/>").html(
            $("<span/>", {'class':"ui-icon ui-icon-alert", style:"float:left; margin:12px 12px 20px 0;"})
        ).append(
            $("<span/>", {id: "dialog-text"})
        )
    );

    $("#" + dialogContainerId + " #dialog-text" ).text(question);
    let buttons = {
        "Cancel": function() {
            $( this ).dialog( "close" );
        }
    };
    buttons[confirmButtonName] = function() {
        $( this ).dialog( "close" );
        onConfirm();
    }
    $("#" + dialogContainerId).dialog({
        title:question,
        buttons: buttons
    });
    $( "#" + dialogContainerId ).dialog('open');
}

function infoDialog(dialogContainerId, text, confirmButtonName, onConfirm) {
    $('#' + dialogContainerId).html(
        $("<p/>").html(
            $("<span/>", {'class':"ui-icon ui-icon-alert", style:"float:left; margin:12px 12px 20px 0;"})
        ).append(
            $("<span/>", {id: "dialog-text"})
        )
    );

    $("#" + dialogContainerId + " #dialog-text" ).text(text);
    let buttons = {};
    buttons[confirmButtonName] = function() {
        $( this ).dialog( "close" );
        onConfirm();
    }
    $("#" + dialogContainerId).dialog({
        title:text,
        buttons: buttons
    });
    $( "#" + dialogContainerId ).dialog('open');
}

function modalDialog(dialogContainerId, title, contentProducer, confirmButtonName, onConfirm, params) {
    let $dialogContainer = $('#' + dialogContainerId);
    $dialogContainer.html(contentProducer());

    let buttons = {};
    buttons[confirmButtonName] = function() {
        $( this ).dialog( "close" );
        onConfirm();
    };
    $dialogContainer.dialog({
        title:title,
        buttons: buttons,
        close: function () {
            if (params && params.onClose) {
                params.onClose();
            }
        }
    });
    $dialogContainer.dialog('open');
}

function generateTranslateSelectionButtons(lang, onTranslate) {
    var $cont = $("<span/>");
    $cont.append(
        $("<button/>", {text: "Google translate"}).click(
            function () {
                let urlPrefix;
                if (lang === "EN") {
                    urlPrefix = "https://translate.google.ru/#view=home&op=translate&sl=en&tl=ru&text="
                } else if (lang === "PL") {
                    urlPrefix = "https://translate.google.ru/#view=home&op=translate&sl=pl&tl=ru&text="
                }
                onTranslate(urlPrefix);
            }
        )
    );
    $cont.append(
        $("<button/>", {text: "Abby translate"}).click(
            function () {
                var urlPrefix;
                if (lang === "EN") {
                    urlPrefix = "https://www.lingvolive.com/ru-ru/translate/en-ru/"
                } else if (lang === "PL") {
                    urlPrefix = "https://www.lingvolive.com/ru-ru/translate/pl-ru/"
                }
                onTranslate(urlPrefix);
            }
        )
    );
    return $cont;
}

function initTranslateSelectionButtons(containerId, lang, onTranslate) {
    $("#" + containerId).html(generateTranslateSelectionButtons(lang, onTranslate));
}

function getSelectedText() {
    return window.getSelection().toString();
}

function clearSelection() {
    if (window.getSelection) {
        if (window.getSelection().empty) {  // Chrome
            window.getSelection().empty();
        } else if (window.getSelection().removeAllRanges) {  // Firefox
            window.getSelection().removeAllRanges();
        }
    }
}

function translateSelection(urlPrefix) {
    translateText(urlPrefix, getSelectedText().trim());
}

function translateText(urlPrefix, word) {
    window.open(urlPrefix + word, '_blank');
}

function strToDivs(str) {
    return _.reduce(
        str.split("\n"),
        function(memo, line){
            return memo.append($("<div/>", {text: line}));
        },
        $("<div/>")
    );
}

function doBackup() {
    confirmCancelDialog("dialog-confirm", "Create backup?", "Yes", function () {
        doPost({
            url:"/backup",
            success: function () {
                infoDialog(
                    "dialog-confirm",
                    "Back was created.",
                    "OK",
                    function () {}
                );

            }
        })
    });
}

function isEmptyString(string) {
    return !string || string.trim().length === 0;
}