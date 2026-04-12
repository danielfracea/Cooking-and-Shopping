// Unit groups with conversion factors relative to a base unit
export const UNIT_GROUPS = {
  weight: {
    baseUnit: 'g',
    units: ['g', 'kg', 'oz', 'lb'],
    toBase: { g: 1, kg: 1000, oz: 28.3495, lb: 453.592 },
  },
  volume: {
    baseUnit: 'mL',
    units: ['mL', 'L', 'tsp', 'tbsp', 'fl oz', 'cup'],
    toBase: { mL: 1, L: 1000, tsp: 4.92892, tbsp: 14.7868, 'fl oz': 29.5735, cup: 236.588 },
  },
  count: {
    baseUnit: 'pcs',
    units: ['pcs', 'dozen'],
    toBase: { pcs: 1, dozen: 12 },
  },
}

// Build a flat map from unit string → group name
const unitToGroupName = {}
for (const [groupName, group] of Object.entries(UNIT_GROUPS)) {
  for (const unit of group.units) {
    unitToGroupName[unit] = groupName
  }
}

export function getUnitGroupName(unit) {
  return unitToGroupName[unit?.trim()] ?? null
}

export function canConvert(fromUnit, toUnit) {
  if (!fromUnit || !toUnit) return false
  if (fromUnit === toUnit) return true
  const fg = getUnitGroupName(fromUnit)
  const tg = getUnitGroupName(toUnit)
  return fg !== null && fg === tg
}

export function convertUnit(value, fromUnit, toUnit) {
  if (!fromUnit || !toUnit || fromUnit === toUnit) return value
  const groupName = getUnitGroupName(fromUnit)
  if (!groupName || getUnitGroupName(toUnit) !== groupName) return value
  const group = UNIT_GROUPS[groupName]
  const baseValue = value * group.toBase[fromUnit]
  return baseValue / group.toBase[toUnit]
}

// Preferred units per system per group
export const SYSTEM_PREFERRED = {
  metric:   { weight: 'kg', volume: 'L',   count: 'pcs' },
  imperial: { weight: 'lb', volume: 'cup', count: 'pcs' },
}

export function getPreferredUnit(unit, system) {
  const groupName = getUnitGroupName(unit)
  if (!groupName) return unit
  return SYSTEM_PREFERRED[system]?.[groupName] ?? unit
}

// Round a numeric quantity to a readable precision
export function formatQuantity(value) {
  const num = parseFloat(value)
  if (isNaN(num)) return String(value ?? '')
  if (num >= 1000) return Math.round(num).toString()
  if (num >= 100)  return parseFloat(num.toFixed(0)).toString()
  if (num >= 10)   return parseFloat(num.toFixed(1)).toString()
  if (num >= 1)    return parseFloat(num.toFixed(2)).toString()
  return parseFloat(num.toFixed(3)).toString()
}

// Flat list of all known units (for dropdowns)
export const ALL_UNITS = Object.values(UNIT_GROUPS).flatMap(g => g.units)

// Items list for v-combobox / v-select with group headers between groups
export const UNIT_SELECT_ITEMS = [
  { title: '── Weight ──', value: '', disabled: true },
  ...UNIT_GROUPS.weight.units.map(u => ({ title: u, value: u })),
  { title: '── Volume ──', value: '', disabled: true },
  ...UNIT_GROUPS.volume.units.map(u => ({ title: u, value: u })),
  { title: '── Count ──', value: '', disabled: true },
  ...UNIT_GROUPS.count.units.map(u => ({ title: u, value: u })),
]
