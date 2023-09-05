/** *******************************************************************
 * copyright (c) 2023 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
module.exports = {
	env: {
		browser: true,
		es6: true,
		node: true
	},
	extends: ['plugin:@typescript-eslint/recommended-type-checked', 'prettier'],
	parser: '@typescript-eslint/parser',
	parserOptions: {
		project: 'tsconfig.json',
		sourceType: 'module'
	},
	plugins: ['eslint-plugin-jsdoc', '@typescript-eslint', '@typescript-eslint/tslint', 'header', 'prettier'],
	root: true,
	rules: {
		'prettier/prettier': 'error',
		'@typescript-eslint/dot-notation': 'error',
		'@typescript-eslint/no-misused-promises': [
			'error',
			{
				checksVoidReturn: {
					arguments: false
				}
			}
		],
		'@typescript-eslint/member-ordering': [
			'error',
			{
				default: [
					'static-field',
					'public-field',
					'instance-field',
					'protected-field',
					'private-field',
					'abstract-field',
					'constructor',
					'public-static-method',
					'protected-static-method',
					'private-static-method',
					'public-method',
					'protected-method',
					'private-method'
				]
			}
		],
		'@typescript-eslint/explicit-function-return-type': [
			'error',
			{
				allowExpressions: false,
				allowTypedFunctionExpressions: false,
				allowHigherOrderFunctions: false,
				allowDirectConstAssertionInArrowFunctions: true,
				allowConciseArrowFunctionExpressionsStartingWithVoid: true
			}
		],
		'@typescript-eslint/explicit-module-boundary-types': [
			'error',
			{
				allowArgumentsExplicitlyTypedAsAny: true,
				allowDirectConstAssertionInArrowFunctions: true,
				allowHigherOrderFunctions: false,
				allowTypedFunctionExpressions: false
			}
		],
		'@typescript-eslint/member-delimiter-style': 'error',
		'@typescript-eslint/naming-convention': [
			'error',
			{
				selector: 'variable',
				format: ['camelCase', 'UPPER_CASE'],
				leadingUnderscore: 'forbid',
				trailingUnderscore: 'forbid'
			}
		],
		'@typescript-eslint/no-empty-function': 'error',
		'@typescript-eslint/no-parameter-properties': 'off',
		'@typescript-eslint/no-explicit-any': 'off',
		'@typescript-eslint/no-unsafe-member-access': 'off',
		'@typescript-eslint/no-unsafe-argument': 'off',
		'@typescript-eslint/no-unsafe-assignment': 'off',
		'@typescript-eslint/no-unsafe-enum-comparison': 'off',
		'@typescript-eslint/restrict-template-expressions': 'off',
		'@typescript-eslint/no-unsafe-return': 'off',
		'@typescript-eslint/no-unsafe-call': 'off',
		'@typescript-eslint/restrict-plus-operands': 'off',
		'@typescript-eslint/no-namespace': 'off',
		'@typescript-eslint/no-unused-expressions': 'off',
		'@typescript-eslint/no-unused-vars': 'error',
		'@typescript-eslint/no-use-before-define': 'error',
		'@typescript-eslint/no-var-requires': 'off',
		'@typescript-eslint/quotes': ['error', 'single'],
		'@typescript-eslint/type-annotation-spacing': 'error',
		'@typescript-eslint/typedef': [
			'error',
			{
				parameter: true,
				propertyDeclaration: true,
				variableDeclaration: true,
				memberVariableDeclaration: true
			}
		],
		'brace-style': ['error', '1tbs'],
		'capitalized-comments': ['error', 'never'],
		'comma-dangle': ['error', 'never'],
		curly: 'error',
		'dot-notation': 'off',
		'eol-last': 'error',
		eqeqeq: ['error', 'smart'],
		'guard-for-in': 'error',
		'id-denylist': 'off',
		'id-match': 'off',
		indent: 'off',
		'jsdoc/check-alignment': 'error',
		'jsdoc/check-indentation': 'error',
		'max-len': [
			'off',
			{
				code: 140
			}
		],
		'no-bitwise': 'error',
		'no-caller': 'error',
		'no-console': [
			'error',
			{
				allow: [
					'log',
					'warn',
					'dir',
					'timeLog',
					'assert',
					'clear',
					'count',
					'countReset',
					'group',
					'groupEnd',
					'table',
					'dirxml',
					'error',
					'groupCollapsed',
					'Console',
					'profile',
					'profileEnd',
					'timeStamp',
					'context'
				]
			}
		],
		'header/header': [
			'error',
			'block',
			[
				'* *******************************************************************',
				{ pattern: ' \\* copyright \\(c\\) [0-9-]{4,9} Red Hat, Inc\\.', template: '* copyright (c) 2023 Red Hat, Inc.' },
				' *',
				' * This program and the accompanying materials are made',
				' * available under the terms of the Eclipse Public License 2.0',
				' * which is available at https://www.eclipse.org/legal/epl-2.0/',
				' *',
				' * SPDX-License-Identifier: EPL-2.0',
				' *********************************************************************'
			]
		],
		'no-debugger': 'error',
		'no-empty': 'error',
		'no-empty-function': 'off',
		'no-eval': 'error',
		'no-fallthrough': 'error',
		'no-new-wrappers': 'error',
		'no-redeclare': 'error',
		'no-trailing-spaces': 'error',
		'no-underscore-dangle': 'off',
		'no-unused-expressions': 'off',
		'no-unused-labels': 'error',
		'no-unused-vars': 'off',
		'no-use-before-define': 'off',
		quotes: 'off',
		radix: 'error',
		semi: 'off',
		'spaced-comment': [
			'error',
			'always',
			{
				markers: ['/']
			}
		]
	}
};
