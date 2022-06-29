package com.drakeet.removesingleline

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Field

/**
 * @author Drakeet Xu
 */
class Hook : IXposedHookLoadPackage {

  override fun handleLoadPackage(l: LoadPackageParam) {

    findAndHookMethod("android.widget.TextView", l.classLoader, "setMaxLines",
      Int::class.javaPrimitiveType,
      object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
          super.beforeHookedMethod(param)
          if (param?.args?.get(0) == 1) {
            param.args[0] = Int.MAX_VALUE
          }
        }
      })

    val singleLineId = l.findField("com.android.internal.R.styleable.TextView_singleLine").getInt(null)
    // Always 32, so maybe we can hardcode it.
    XposedBridge.log("TextView_singleLine: $singleLineId")

    findAndHookMethod("android.content.res.TypedArray", l.classLoader, "getBoolean",
      Int::class.javaPrimitiveType,
      Boolean::class.javaPrimitiveType,
      object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam?) {
          super.beforeHookedMethod(param)
          val f = param?.args?.get(0)
          if (f == singleLineId) {
            param.result = false
          }
        }
      })
  }

  private fun LoadPackageParam.findField(classAndFieldName: String): Field {
    val cfIndex = classAndFieldName.lastIndexOf(".")
    val className = classAndFieldName.substring(0, cfIndex)
    val fieldName = classAndFieldName.substring(cfIndex + 1)
    return findField(findClass(className, classLoader), fieldName)
  }
}
