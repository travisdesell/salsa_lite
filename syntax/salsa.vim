" Vim syntax file
" Language:     Salsa
" Maintainer:   Claudio Fleiner <claudio@fleiner.com>
" URL:		http://www.fleiner.com/vim/syntax/salsa.vim
" Last Change:  2007 Dec 21

" Please check :help salsa.vim for comments on some of the options available.

" Quit when a syntax file was already loaded
if !exists("main_syntax")
  if version < 600
    syntax clear
  elseif exists("b:current_syntax")
    finish
  endif
  " we define it here so that included files can test for it
  let main_syntax='salsa'
endif

" don't use standard HiLink, it will not work with included syntax files
if version < 508
  command! -nargs=+ SalsaHiLink hi link <args>
else
  command! -nargs=+ SalsaHiLink hi def link <args>
endif

" some characters that cannot be in a salsa program (outside a string)
syn match salsaError "[\\@`]"
syn match salsaError "<<<\|\.\.\|=>\|<>\|||=\|&&=\|[^-]->\|\*\/"
syn match salsaOK "\.\.\."

" use separate name so that it can be deleted in salsacc.vim
syn match   salsaError2 "#\|=<"
SalsaHiLink salsaError2 salsaError



" keyword definitions
syn keyword salsaExternal	native module 
syn match salsaExternal		"\<import\>\(\s\+static\>\)\?"
syn keyword salsaError		goto const
syn keyword salsaConditional	if else switch
syn keyword salsaRepeat		while for do
syn keyword salsaBoolean		true false
syn keyword salsaConstant	null
syn keyword salsaTypedef		this super
syn keyword salsaOperator	new instanceof
syn keyword salsaType		boolean char byte short int long float double
syn keyword salsaType		ack
syn keyword salsaStatement	return
syn keyword salsaStorageClass	static synchronized transient volatile final strictfp serializable
syn keyword salsaExceptions	throw try catch finally
syn keyword salsaAssert		assert
syn keyword salsaMethodDecl	synchronized throws
syn keyword salsaClassDecl	extends implements interface
" to differentiate the keyword class from MyClass.class we use a match here
syn match   salsaTypedef		"\.\s*\<behavior\>"ms=s+1
syn keyword salsaClassDecl	enum
syn match   salsaClassDecl	"^behavior\>"
syn match   salsaClassDecl	"[^.]\s*\<behavior\>"ms=s+1
syn match   salsaAnnotation      "@[_$a-zA-Z][_$a-zA-Z0-9_]*\>"
syn match   salsaClassDecl       "@interface\>"
syn keyword salsaBranch		break continue nextgroup=salsaUserLabelRef skipwhite
syn match   salsaUserLabelRef	"\k\+" contained
syn match   salsaVarArg		"\.\.\."
syn keyword salsaScopeDecl	public protected private abstract

if exists("salsa_highlight_salsa_lang_ids")
  let salsa_highlight_all=1
endif
if exists("salsa_highlight_all")  || exists("salsa_highlight_salsa")  || exists("salsa_highlight_salsa_lang")
  " salsa.lang.*
  syn match salsaLangClass "\<System\>"
  syn keyword salsaR_SalsaLang NegativeArraySizeException ArrayStoreException IllegalStateException RuntimeException IndexOutOfBoundsException UnsupportedOperationException ArrayIndexOutOfBoundsException ArithmeticException ClassCastException EnumConstantNotPresentException StringIndexOutOfBoundsException IllegalArgumentException IllegalMonitorStateException IllegalThreadStateException NumberFormatException NullPointerException TypeNotPresentException SecurityException
  syn cluster salsaTop add=salsaR_SalsaLang
  syn cluster salsaClasses add=salsaR_SalsaLang
  SalsaHiLink salsaR_SalsaLang salsaR_Salsa
  syn keyword salsaC_SalsaLang Process RuntimePermission StringKeySet CharacterData01 Class ThreadLocal ThreadLocalMap CharacterData0E Package Character StringCoding Long ProcessImpl ProcessEnvironment Short AssertionStatusDirectives 1PackageInfoProxy UnicodeBlock InheritableThreadLocal AbstractStringBuilder StringEnvironment ClassLoader ConditionalSpecialCasing CharacterDataPrivateUse StringBuffer StringDecoder Entry StringEntry WrappedHook StringBuilder StrictMath State ThreadGroup Runtime CharacterData02 MethodArray Object CharacterDataUndefined Integer Gate Boolean Enum Variable Subset StringEncoder Void Terminator CharsetSD IntegerCache CharacterCache Byte CharsetSE Thread SystemClassLoaderAction CharacterDataLatin1 StringValues StackTraceElement Shutdown ShortCache String ConverterSD ByteCache Lock EnclosingMethodInfo Math Float Value Double SecurityManager LongCache ProcessBuilder StringEntrySet Compiler Number UNIXProcess ConverterSE ExternalData CaseInsensitiveComparator CharacterData00 NativeLibrary
  syn cluster salsaTop add=salsaC_SalsaLang
  syn cluster salsaClasses add=salsaC_SalsaLang
  SalsaHiLink salsaC_SalsaLang salsaC_Salsa
  syn keyword salsaE_SalsaLang IncompatibleClassChangeError InternalError UnknownError ClassCircularityError AssertionError ThreadDeath IllegalAccessError NoClassDefFoundError ClassFormatError UnsupportedClassVersionError NoSuchFieldError VerifyError ExceptionInInitializerError InstantiationError LinkageError NoSuchMethodError Error UnsatisfiedLinkError StackOverflowError AbstractMethodError VirtualMachineError OutOfMemoryError
  syn cluster salsaTop add=salsaE_SalsaLang
  syn cluster salsaClasses add=salsaE_SalsaLang
  SalsaHiLink salsaE_SalsaLang salsaE_Salsa
  syn keyword salsaX_SalsaLang CloneNotSupportedException Exception NoSuchMethodException IllegalAccessException NoSuchFieldException Throwable InterruptedException ClassNotFoundException InstantiationException
  syn cluster salsaTop add=salsaX_SalsaLang
  syn cluster salsaClasses add=salsaX_SalsaLang
  SalsaHiLink salsaX_SalsaLang salsaX_Salsa

  SalsaHiLink salsaR_Salsa salsaR_
  SalsaHiLink salsaC_Salsa salsaC_
  SalsaHiLink salsaE_Salsa salsaE_
  SalsaHiLink salsaX_Salsa salsaX_
  SalsaHiLink salsaX_		     salsaExceptions
  SalsaHiLink salsaR_		     salsaExceptions
  SalsaHiLink salsaE_		     salsaExceptions
  SalsaHiLink salsaC_		     salsaConstant

  syn keyword salsaLangObject clone equals finalize getClass hashCode
  syn keyword salsaLangObject notify notifyAll toString wait
  SalsaHiLink salsaLangObject		     salsaConstant
  syn cluster salsaTop add=salsaLangObject
endif

if filereadable(expand("<sfile>:p:h")."/salsaid.vim")
  source <sfile>:p:h/salsaid.vim
endif

if exists("salsa_space_errors")
  if !exists("salsa_no_trail_space_error")
    syn match   salsaSpaceError  "\s\+$"
  endif
  if !exists("salsa_no_tab_space_error")
    syn match   salsaSpaceError  " \+\t"me=e-1
  endif
endif

syn region  salsaLabelRegion     transparent matchgroup=salsaLabel start="\<case\>" matchgroup=NONE end=":" contains=salsaNumber,salsaCharacter
syn match   salsaUserLabel       "^\s*[_$a-zA-Z][_$a-zA-Z0-9_]*\s*:"he=e-1 contains=salsaLabel
syn keyword salsaLabel		default

if !exists("salsa_allow_cpp_keywords")
  " The default used to be to highlight C++ keywords.  But several people
  " don't like that, so default to not highlighting these.
  let salsa_allow_cpp_keywords = 1
endif
if !salsa_allow_cpp_keywords
  syn keyword salsaError auto delete extern friend inline redeclared
  syn keyword salsaError register signed sizeof struct template typedef union
  syn keyword salsaError unsigned operator
endif

" The following cluster contains all salsa groups except the contained ones
syn cluster salsaTop add=salsaExternal,salsaError,salsaError,salsaBranch,salsaLabelRegion,salsaLabel,salsaConditional,salsaRepeat,salsaBoolean,salsaConstant,salsaTypedef,salsaOperator,salsaType,salsaType,salsaStatement,salsaStorageClass,salsaAssert,salsaExceptions,salsaMethodDecl,salsaClassDecl,salsaClassDecl,salsaClassDecl,salsaScopeDecl,salsaError,salsaError2,salsaUserLabel,salsaLangObject,salsaAnnotation,salsaVarArg


" Comments
syn keyword salsaTodo		 contained TODO FIXME XXX
if exists("salsa_comment_strings")
  syn region  salsaCommentString    contained start=+"+ end=+"+ end=+$+ end=+\*/+me=s-1,he=s-1 contains=salsaSpecial,salsaCommentStar,salsaSpecialChar,@Spell
  syn region  salsaComment2String   contained start=+"+  end=+$\|"+  contains=salsaSpecial,salsaSpecialChar,@Spell
  syn match   salsaCommentCharacter contained "'\\[^']\{1,6\}'" contains=salsaSpecialChar
  syn match   salsaCommentCharacter contained "'\\''" contains=salsaSpecialChar
  syn match   salsaCommentCharacter contained "'[^\\]'"
  syn cluster salsaCommentSpecial add=salsaCommentString,salsaCommentCharacter,salsaNumber
  syn cluster salsaCommentSpecial2 add=salsaComment2String,salsaCommentCharacter,salsaNumber
endif
syn region  salsaComment		 start="/\*"  end="\*/" contains=@salsaCommentSpecial,salsaTodo,@Spell
syn match   salsaCommentStar      contained "^\s*\*[^/]"me=e-1
syn match   salsaCommentStar      contained "^\s*\*$"
syn match   salsaLineComment      "//.*" contains=@salsaCommentSpecial2,salsaTodo,@Spell
SalsaHiLink salsaCommentString salsaString
SalsaHiLink salsaComment2String salsaString
SalsaHiLink salsaCommentCharacter salsaCharacter

syn cluster salsaTop add=salsaComment,salsaLineComment

if !exists("salsa_ignore_salsadoc") && main_syntax != 'jsp'
  syntax case ignore
  " syntax coloring for salsadoc comments (HTML)
  syntax include @salsaHtml <sfile>:p:h/html.vim
  unlet b:current_syntax
  syn region  salsaDocComment    start="/\*\*"  end="\*/" keepend contains=salsaCommentTitle,@salsaHtml,salsaDocTags,salsaDocSeeTag,salsaTodo,@Spell
  syn region  salsaCommentTitle  contained matchgroup=salsaDocComment start="/\*\*"   matchgroup=salsaCommentTitle keepend end="\.$" end="\.[ \t\r<&]"me=e-1 end="[^{]@"me=s-2,he=s-1 end="\*/"me=s-1,he=s-1 contains=@salsaHtml,salsaCommentStar,salsaTodo,@Spell,salsaDocTags,salsaDocSeeTag

  syn region salsaDocTags         contained start="{@\(link\|linkplain\|inherit[Dd]oc\|doc[rR]oot\|value\)" end="}"
  syn match  salsaDocTags         contained "@\(param\|exception\|throws\|since\)\s\+\S\+" contains=salsaDocParam
  syn match  salsaDocParam        contained "\s\S\+"
  syn match  salsaDocTags         contained "@\(version\|author\|return\|deprecated\|serial\|serialField\|serialData\)\>"
  syn region salsaDocSeeTag       contained matchgroup=salsaDocTags start="@see\s\+" matchgroup=NONE end="\_."re=e-1 contains=salsaDocSeeTagParam
  syn match  salsaDocSeeTagParam  contained @"\_[^"]\+"\|<a\s\+\_.\{-}</a>\|\(\k\|\.\)*\(#\k\+\((\_[^)]\+)\)\=\)\=@ extend
  syntax case match
endif

" match the special comment /**/
syn match   salsaComment		 "/\*\*/"

" Strings and constants
syn match   salsaSpecialError     contained "\\."
syn match   salsaSpecialCharError contained "[^']"
syn match   salsaSpecialChar      contained "\\\([4-9]\d\|[0-3]\d\d\|[\"\\'ntbrf]\|u\x\{4\}\)"
syn region  salsaString		start=+"+ end=+"+ end=+$+ contains=salsaSpecialChar,salsaSpecialError,@Spell
" next line disabled, it can cause a crash for a long line
"syn match   salsaStringError	  +"\([^"\\]\|\\.\)*$+
syn match   salsaCharacter	 "'[^']*'" contains=salsaSpecialChar,salsaSpecialCharError
syn match   salsaCharacter	 "'\\''" contains=salsaSpecialChar
syn match   salsaCharacter	 "'[^\\]'"
syn match   salsaNumber		 "\<\(0[0-7]*\|0[xX]\x\+\|\d\+\)[lL]\=\>"
syn match   salsaNumber		 "\(\<\d\+\.\d*\|\.\d\+\)\([eE][-+]\=\d\+\)\=[fFdD]\="
syn match   salsaNumber		 "\<\d\+[eE][-+]\=\d\+[fFdD]\=\>"
syn match   salsaNumber		 "\<\d\+\([eE][-+]\=\d\+\)\=[fFdD]\>"

" unicode characters
syn match   salsaSpecial "\\u\d\{4\}"

syn cluster salsaTop add=salsaString,salsaCharacter,salsaNumber,salsaSpecial,salsaStringError

if exists("salsa_highlight_functions")
  if salsa_highlight_functions == "indent"
    syn match  salsaFuncDef "^\(\t\| \{8\}\)[_$a-zA-Z][_$a-zA-Z0-9_. \[\]]*([^-+*/()]*)" contains=salsaScopeDecl,salsaType,salsaStorageClass,@salsaClasses
    syn region salsaFuncDef start=+^\(\t\| \{8\}\)[$_a-zA-Z][$_a-zA-Z0-9_. \[\]]*([^-+*/()]*,\s*+ end=+)+ contains=salsaScopeDecl,salsaType,salsaStorageClass,@salsaClasses
    syn match  salsaFuncDef "^  [$_a-zA-Z][$_a-zA-Z0-9_. \[\]]*([^-+*/()]*)" contains=salsaScopeDecl,salsaType,salsaStorageClass,@salsaClasses
    syn region salsaFuncDef start=+^  [$_a-zA-Z][$_a-zA-Z0-9_. \[\]]*([^-+*/()]*,\s*+ end=+)+ contains=salsaScopeDecl,salsaType,salsaStorageClass,@salsaClasses
  else
    " This line catches method declarations at any indentation>0, but it assumes
    " two things:
    "   1. class names are always capitalized (ie: Button)
    "   2. method names are never capitalized (except constructors, of course)
    syn region salsaFuncDef start=+^\s\+\(\(public\|protected\|private\|static\|abstract\|final\|native\|synchronized\)\s\+\)*\(\(void\|boolean\|char\|byte\|short\|int\|long\|float\|double\|\([A-Za-z_][A-Za-z0-9_$]*\.\)*[A-Z][A-Za-z0-9_$]*\)\(<[^>]*>\)\=\(\[\]\)*\s\+[a-z][A-Za-z0-9_$]*\|[A-Z][A-Za-z0-9_$]*\)\s*([^0-9]+ end=+)+ contains=salsaScopeDecl,salsaType,salsaStorageClass,salsaComment,salsaLineComment,@salsaClasses
  endif
  syn match  salsaBraces  "[{}]"
  syn cluster salsaTop add=salsaFuncDef,salsaBraces
endif

if exists("salsa_highlight_debug")

  " Strings and constants
  syn match   salsaDebugSpecial		contained "\\\d\d\d\|\\."
  syn region  salsaDebugString		contained start=+"+  end=+"+  contains=salsaDebugSpecial
  syn match   salsaDebugStringError      +"\([^"\\]\|\\.\)*$+
  syn match   salsaDebugCharacter	contained "'[^\\]'"
  syn match   salsaDebugSpecialCharacter contained "'\\.'"
  syn match   salsaDebugSpecialCharacter contained "'\\''"
  syn match   salsaDebugNumber		contained "\<\(0[0-7]*\|0[xX]\x\+\|\d\+\)[lL]\=\>"
  syn match   salsaDebugNumber		contained "\(\<\d\+\.\d*\|\.\d\+\)\([eE][-+]\=\d\+\)\=[fFdD]\="
  syn match   salsaDebugNumber		contained "\<\d\+[eE][-+]\=\d\+[fFdD]\=\>"
  syn match   salsaDebugNumber		contained "\<\d\+\([eE][-+]\=\d\+\)\=[fFdD]\>"
  syn keyword salsaDebugBoolean		contained true false
  syn keyword salsaDebugType		contained null this super
  syn region salsaDebugParen  start=+(+ end=+)+ contained contains=salsaDebug.*,salsaDebugParen

  " to make this work you must define the highlighting for these groups
  syn match salsaDebug "\<System\.\(out\|err\)\.print\(ln\)*\s*("me=e-1 contains=salsaDebug.* nextgroup=salsaDebugParen
  syn match salsaDebug "\<p\s*("me=e-1 contains=salsaDebug.* nextgroup=salsaDebugParen
  syn match salsaDebug "[A-Za-z][a-zA-Z0-9_]*\.printStackTrace\s*("me=e-1 contains=salsaDebug.* nextgroup=salsaDebugParen
  syn match salsaDebug "\<trace[SL]\=\s*("me=e-1 contains=salsaDebug.* nextgroup=salsaDebugParen

  syn cluster salsaTop add=salsaDebug

  if version >= 508 || !exists("did_c_syn_inits")
    SalsaHiLink salsaDebug		 Debug
    SalsaHiLink salsaDebugString		 DebugString
    SalsaHiLink salsaDebugStringError	 salsaError
    SalsaHiLink salsaDebugType		 DebugType
    SalsaHiLink salsaDebugBoolean		 DebugBoolean
    SalsaHiLink salsaDebugNumber		 Debug
    SalsaHiLink salsaDebugSpecial		 DebugSpecial
    SalsaHiLink salsaDebugSpecialCharacter DebugSpecial
    SalsaHiLink salsaDebugCharacter	 DebugString
    SalsaHiLink salsaDebugParen		 Debug

    SalsaHiLink DebugString		 String
    SalsaHiLink DebugSpecial		 Special
    SalsaHiLink DebugBoolean		 Boolean
    SalsaHiLink DebugType		 Type
  endif
endif

if exists("salsa_mark_braces_in_parens_as_errors")
  syn match salsaInParen		 contained "[{}]"
  SalsaHiLink salsaInParen	salsaError
  syn cluster salsaTop add=salsaInParen
endif

" catch errors caused by wrong parenthesis
syn region  salsaParenT  transparent matchgroup=salsaParen  start="("  end=")" contains=@salsaTop,salsaParenT1
syn region  salsaParenT1 transparent matchgroup=salsaParen1 start="(" end=")" contains=@salsaTop,salsaParenT2 contained
syn region  salsaParenT2 transparent matchgroup=salsaParen2 start="(" end=")" contains=@salsaTop,salsaParenT  contained
syn match   salsaParenError       ")"
" catch errors caused by wrong square parenthesis
syn region  salsaParenT  transparent matchgroup=salsaParen  start="\["  end="\]" contains=@salsaTop,salsaParenT1
syn region  salsaParenT1 transparent matchgroup=salsaParen1 start="\[" end="\]" contains=@salsaTop,salsaParenT2 contained
syn region  salsaParenT2 transparent matchgroup=salsaParen2 start="\[" end="\]" contains=@salsaTop,salsaParenT  contained
syn match   salsaParenError       "\]"

SalsaHiLink salsaParenError       salsaError

if !exists("salsa_minlines")
  let salsa_minlines = 10
endif
exec "syn sync ccomment salsaComment minlines=" . salsa_minlines

" The default highlighting.
if version >= 508 || !exists("did_salsa_syn_inits")
  if version < 508
    let did_salsa_syn_inits = 1
  endif
  SalsaHiLink salsaFuncDef		Function
  SalsaHiLink salsaVarArg                 Function
  SalsaHiLink salsaBraces			Function
  SalsaHiLink salsaBranch			Conditional
  SalsaHiLink salsaUserLabelRef		salsaUserLabel
  SalsaHiLink salsaLabel			Label
  SalsaHiLink salsaUserLabel		Label
  SalsaHiLink salsaConditional		Conditional
  SalsaHiLink salsaRepeat			Repeat
  SalsaHiLink salsaExceptions		Exception
  SalsaHiLink salsaAssert			Statement
  SalsaHiLink salsaStorageClass		StorageClass
  SalsaHiLink salsaMethodDecl		salsaStorageClass
  SalsaHiLink salsaClassDecl		salsaStorageClass
  SalsaHiLink salsaScopeDecl		salsaStorageClass
  SalsaHiLink salsaBoolean		Boolean
  SalsaHiLink salsaSpecial		Special
  SalsaHiLink salsaSpecialError		Error
  SalsaHiLink salsaSpecialCharError	Error
  SalsaHiLink salsaString			String
  SalsaHiLink salsaCharacter		Character
  SalsaHiLink salsaSpecialChar		SpecialChar
  SalsaHiLink salsaNumber			Number
  SalsaHiLink salsaError			Error
  SalsaHiLink salsaStringError		Error
  SalsaHiLink salsaStatement		Statement
  SalsaHiLink salsaOperator		Operator
  SalsaHiLink salsaComment		Comment
  SalsaHiLink salsaDocComment		Comment
  SalsaHiLink salsaLineComment		Comment
  SalsaHiLink salsaConstant		Constant
  SalsaHiLink salsaTypedef		Typedef
  SalsaHiLink salsaTodo			Todo
  SalsaHiLink salsaAnnotation             PreProc

  SalsaHiLink salsaCommentTitle		SpecialComment
  SalsaHiLink salsaDocTags		Special
  SalsaHiLink salsaDocParam		Function
  SalsaHiLink salsaDocSeeTagParam		Function
  SalsaHiLink salsaCommentStar		salsaComment

  SalsaHiLink salsaType			Type
  SalsaHiLink salsaExternal		Include

  SalsaHiLink htmlComment		Special
  SalsaHiLink htmlCommentPart		Special
  SalsaHiLink salsaSpaceError		Error
endif

delcommand SalsaHiLink

let b:current_syntax = "salsa"

if main_syntax == 'salsa'
  unlet main_syntax
endif

let b:spell_options="contained"

" vim: ts=8
