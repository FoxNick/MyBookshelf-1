!function () {
    return function e(t, n, r) {
        function i(a, s) {
            if (!n[a]) {
                if (!t[a]) {
                    var c = "function" == typeof require && require;
                    if (!s && c) return c(a, !0);
                    if (o) return o(a, !0);
                    var l = new Error("Cannot find module '" + a + "'");
                    throw l.code = "MODULE_NOT_FOUND", l
                }
                var d = n[a] = {exports: {}};
                t[a][0].call(d.exports, function (e) {
                    return i(t[a][1][e] || e)
                }, d, d.exports, e, t, n, r)
            }
            return n[a].exports
        }

        for (var o = "function" == typeof require && require, a = 0; a < r.length; a++) i(r[a]);
        return i
    }
}()({
    1: [function (e, t, n) {
        !function () {
            "use strict";
            var t, n, r = e("./editor-libs/feature-detector.js"), i = e("./editor-libs/console"),
                o = e("./editor-libs/events.js"), a = e("./editor-libs/mce-utils"),
                s = document.getElementById("static-js"), c = s.dataset.feature, execute = document.getElementById("execute"),
                d = document.querySelector("#console code"), reset = document.getElementById("reset"),reload = document.getElementById("reload");

            function f() {
                !function (e) {
                    d.classList.add("fade-in");
                    try {
                        new Function(e)()
                    } catch (e) {
                        d.textContent = "Error: " + e.message
                    }
                    d.addEventListener("animationend", function () {
                        d.classList.remove("fade-in")
                    })
                }(t.getDoc().getValue())
            }

            !document.all && r.isDefined(c) && (document.documentElement.classList.add("js"), s.dataset.height && document.getElementById("editor").classList.add(s.dataset.height), document.getElementById("static").classList.add("hidden"), document.getElementById("live").classList.remove("hidden"), i(), o.register(), n = document.getElementById("editor"), t = CodeMirror(n, {
                autofocus: !0,
                inputStyle: "contenteditable",
                lineNumbers: !0,
                mode: "javascript",
                undoDepth: 5,
                tabindex: 0,
                value: s.textContent
            }), execute.addEventListener("click", function () {
                d.textContent = "", f()
            }), reset.addEventListener("click", function () {
                    d.textContent = "";
                }),reload.addEventListener("click", function () {
                    window.location.reload();
                })

            ), void 0 !== performance && document.addEventListener("readystatechange", function (e) {
                "complete" === e.target.readyState && setTimeout(function () {
                    o.trackloadEventEnd("JS editor load time", performance.timing.loadEventEnd), a.postToKuma({markName: "js-ie-load-event-end"})
                }, 300)
            })
        }()
    }, {
        "./editor-libs/console": 5,
        "./editor-libs/events.js": 7,
        "./editor-libs/feature-detector.js": 8,
        "./editor-libs/mce-utils": 9
    }], 2: [function (e, t, n) {
        t.exports = {
            trackEvent: function (e) {
            }, trackCSSExampleSelection: function () {
                this.trackEvent({
                    category: "Interactive Example - CSS",
                    action: "New CSS example selected",
                    label: "Interaction Events"
                })
            }, trackRunClicks: function () {
                "use strict";
                this.trackEvent({
                    category: "Interactive Example - JS",
                    action: "Clicked run",
                    label: "Interaction Events"
                })
            }
        }
    }, {}], 3: [function (e, t, n) {
        var r = e("./mce-utils");
        t.exports = {
            addClippy: function () {
                "use strict";
                new Clipboard(".copy", {
                    target: function (e) {
                        var t = e.dataset.clipboardTarget;
                        return t ? document.querySelector(t) : r.findParentChoiceElem(e).getElementsByTagName("code")[0]
                    }
                }).on("success", function (e) {
                    var t = document.getElementById("user-message");
                    t.classList.add("show"), t.setAttribute("aria-hidden", !1), function (e, t) {
                        var n = e.trigger, r = n.offsetParent.offsetTop + n.clientHeight + 10 + "px",
                            i = n.offsetLeft + "px";
                        t.style.top = r, t.style.left = i
                    }(e, t), window.setTimeout(function () {
                        t.classList.remove("show"), t.setAttribute("aria-hidden", !0)
                    }, 1e3), e.clearSelection()
                })
            }, toggleClippy: function (e) {
                "use strict";
                for (var t = e.querySelector(".copy"), n = document.querySelectorAll(".copy"), r = 0, i = n.length; r < i; r++) n[r].classList.add("hidden"), n[r].setAttribute("aria-hidden", !0);
                t.classList.remove("hidden"), t.setAttribute("aria-hidden", !1)
            }
        }
    }, {"./mce-utils": 9}], 4: [function (e, t, n) {
        t.exports = {
            formatArray: function (e) {
                "use strict";
                for (var t = "", n = 0, r = e.length; n < r; n++) "string" == typeof e[n] ? t += '"' + e[n] + '"' : Array.isArray(e[n]) ? (t += "Array [", t += this.formatArray(e[n]), t += "]") : t += this.formatOutput(e[n]), n < e.length - 1 && (t += ", ");
                return t
            }, formatObject: function (e) {
                "use strict";
                var t = e.constructor.name;
                if ("String" === t) return `String{"${e.valueOf()}"}`;
                if (t.match(/^(ArrayBuffer|SharedArrayBuffer|DataView)$/)) return t + " {}";
                if (t.match(/^(Int8Array|Int16Array|Int32Array|Uint8Array|Uint16Array|Uint32Array|Uint8ClampedArray|Float32Array|Float64Array)$/)) return e.length > 0 ? t + " [" + this.formatArray(e) + "]" : t + " []";
                if ("Symbol" === t && void 0 !== e) return e.toString();
                if ("Object" === t) {
                    var n = "", r = !0;
                    for (var i in e) e.hasOwnProperty(i) && (r ? r = !1 : n += ", ", n = n + i + ": " + this.formatOutput(e[i]));
                    return t + " { " + n + " }"
                }
                return e
            }, formatOutput: function (e) {
                "use strict";
                return void 0 === e || null === e || "boolean" == typeof e ? String(e) : "number" == typeof e ? Object.is(e, -0) ? "-0" : String(e) : "string" == typeof e ? '"' + e + '"' : Array.isArray(e) ? "Array [" + this.formatArray(e) + "]" : this.formatObject(e)
            }, writeOutput: function (e) {
                "use strict";
                var t = document.querySelector("#console code"), n = t.textContent, r = "> " + e + "\n";
                t.textContent = n + r
            }
        }
    }, {}], 5: [function (e, t, n) {
        t.exports = function () {
            "use strict";
            var t = e("./console-utils"), n = console.log, r = console.error;
            console.error = function (e) {
                t.writeOutput(e), r.apply(console, arguments)
            }, console.log = function () {
                for (var e = [], r = 0, i = arguments.length; r < i; r++) {
                    var o = t.formatOutput(arguments[r]);
                    e.push(o)
                }
                var a = e.join(" ");
                t.writeOutput(a), n.apply(console, arguments)
            }
        }
    }, {"./console-utils": 4}], 6: [function (e, t, n) {
        t.exports = {
            editTimer: void 0, applyCode: function (e, t, n) {
                var r = n || document.getElementById("example-element");
                e.replace(/(\/\*)[\s\S]+(\*\/)/g, ""), r.style.cssText = e, clearTimeout(this.editTimer), this.editTimer = setTimeout(function () {
                    r.style.cssText ? t.parentNode.classList.remove("invalid") : t.parentNode.classList.add("invalid")
                }, 500)
            }, choose: function (e) {
                var n = e.querySelector("code");
                e.classList.add("selected"), n.setAttribute("contentEditable", !0), n.setAttribute("spellcheck", !1), t.exports.applyCode(n.textContent, e)
            }, resetDefault: function () {
                var e = document.getElementById("default-example"), n = document.getElementById("output");
                if (e.classList.contains("hidden")) {
                    for (var r = n.querySelectorAll("section"), i = 0, o = r.length; i < o; i++) r[i].classList.add("hidden"), r[i].setAttribute("aria-hidden", !0);
                    e.classList.remove("hidden"), e.setAttribute("aria-hidden", !1)
                }
                t.exports.resetUIState()
            }, resetUIState: function () {
                for (var e = document.getElementById("example-choice-list").querySelectorAll(".example-choice"), t = 0, n = e.length; t < n; t++) e[t].classList.remove("selected")
            }
        }
    }, {}], 7: [function (e, t, n) {
        var r = e("./clippy"), i = e("./css-editor-utils"), o = e("./analytics"), a = e("./mce-utils");

        function s(e) {
            "use strict";
            var t = window.getSelection().getRangeAt(0);
            e.preventDefault(), e.stopPropagation(), e.clipboardData.setData("text/plain", t.toString()), e.clipboardData.setData("text/html", t.toString())
        }

        function c(e) {
            "use strict";
            var t = e.clipboardData.getData("text/plain"), n = e.target.offsetParent.querySelector("code"),
                r = n.textContent;
            e.preventDefault(), e.stopPropagation(), n.innerText = r + "\n" + t, Prism.highlightElement(n)
        }

        t.exports = {
            onChoose: function (e) {
                var t = document.querySelector(".selected");
                if (t && !e.classList.contains("selected")) {
                    var n = Prism.highlight(t.firstChild.textContent, Prism.languages.css);
                    t.firstChild.innerHTML = n, o.trackCSSExampleSelection(), i.resetDefault()
                }
                i.choose(e), r.toggleClippy(e)
            }, register: function () {
                "use strict";
                var e = document.getElementById("example-choice-list"), n = document.getElementById("editor");
                !function () {
                    window.onerror = function (e, t, n, r, i) {
                        var a = ["URL: " + t, "Line: " + n, "Column: " + r, "Error object: " + JSON.stringify(i)].join(" - ");
                        o.trackEvent({category: "Interactive Example - JavaScript Errors", action: a, label: e})
                    }
                }(), e && (function () {
                    window.addEventListener("message", function (e) {
                        var t = window.ieConfig && window.ieConfig.origin ? window.ieConfig.origin : "https://developer.mozilla.org";
                        if (e.origin === t && void 0 !== typeof e.data.smallViewport) {
                            var n = document.querySelector(".editor-wrapper");
                            e.data.smallViewport ? n.classList.add("small-desktop-and-below") : n.classList.remove("small-desktop-and-below")
                        }
                    }, !1)
                }(), function (e) {
                    e.addEventListener("cut", s), e.addEventListener("copy", s), e.addEventListener("paste", c), e.addEventListener("keyup", function (e) {
                        var t = e.target.parentElement;
                        i.applyCode(t.textContent, t)
                    }), e.addEventListener("click", function (e) {
                        var n = e.target;
                        n.classList.contains("example-choice") || (n = a.findParentChoiceElem(n)), n.classList.contains("copy") && o.trackEvent({
                            category: "Interactive Example - CSS",
                            action: "Copy to clipboard clicked",
                            label: "Interaction Events"
                        }), t.exports.onChoose(n)
                    })
                }(e)), n && function (e) {
                    e.addEventListener("click", function (e) {
                        "execute" === e.target.id && o.trackRunClicks()
                    })
                }(n)
            }, trackloadEventEnd: function (e, t) {
                o.trackEvent({category: "Interactive Examples", action: e, label: "Performance Events", value: t})
            }
        }
    }, {"./analytics": 2, "./clippy": 3, "./css-editor-utils": 6, "./mce-utils": 9}], 8: [function (e, t, n) {
        t.exports = {
            isDefined: function (e) {
                "use strict";
                return void 0 === e || void 0 !== function (e) {
                    var t = void 0;
                    switch (e) {
                        case"array-entries":
                            t = Array.prototype.entries;
                            break;
                        case"shared-array-buffer":
                            t = window.SharedArrayBuffer
                    }
                    return t
                }(e)
            }
        }
    }, {}], 9: [function (e, t, n) {
        t.exports = {
            findParentChoiceElem: function (e) {
                "use strict";
                for (var t = e.parentElement, n = t.classList; t && !n.contains("example-choice");) n = (t = t.parentElement).classList;
                return t
            }, isPropertySupported: function (e) {
                "use strict";
                if (void 0 === e.property) return !0;
                for (var t = e.property.split(" "), n = !1, r = document.createElement("div"), i = 0, o = t.length; i < o; i++) void 0 !== r.style[t[i]] && (n = !0);
                return n
            }, openLinksInNewTab: function (e) {
                e.forEach(function (e) {
                    e.addEventListener("click", function (t) {
                        t.preventDefault(), window.open(e.href)
                    })
                })
            }, postToKuma: function (e) {
            }, scrollToAnchors: function (e, t) {
                t.forEach(function (t) {
                    t.addEventListener("click", function (n) {
                        n.preventDefault(), e.querySelector(t.hash).scrollIntoView()
                    })
                })
            }, showCustomExampleHTML: function (e) {
                "use strict";
                var t = document.getElementById("default-example");
                t.classList.add("hidden"), t.setAttribute("aria-hidden", !0), e.classList.remove("hidden"), e.setAttribute("aria-hidden", !1)
            }
        }
    }, {}]
}, {}, [1]);