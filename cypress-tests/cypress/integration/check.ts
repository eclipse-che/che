type Url = string
it('loads examples', () => {
  const url: Url = 'https://example.cypress.io'
  cy.visit(url)
  cy.contains('Kitchen Sink')
})
