const nbTwoWall = {
  unit: {
    name: 'Wall',
    code: '6-n-f2-r0',
    rate: 0,
    number: 1,
    scale: 1,
    origin: { x: 0, y: 0 },
  },
  outline: [
    {
      x: 141.2, y: 167.7, radius: 0, curve: 'none', index: 0,
    },
    {
      x: 179.3, y: 167.7, radius: 0, curve: 'none', index: 1,
    },
    {
      x: 179.3, y: 218.7, radius: 0, curve: 'none', index: 2,
    },
    {
      x: 217.6, y: 218.8, radius: 0, curve: 'none', index: 3,
    },
    {
      x: 217.6, y: 478.2, radius: 0, curve: 'none', index: 4,
    },
    {
      x: 179, y: 478.2, radius: 0, curve: 'none', index: 5,
    },
    {
      x: 179, y: 498.8, radius: 0, curve: 'none', index: 6,
    },
    {
      x: 141.2, y: 498.8, radius: 0, curve: 'none', index: 7,
    },
  ],
  features: [
    {
      type: 'interiorWall',
      code: '6-n-f2-r0-int1',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        { x: 184.8, y: 213, radius: 0, curve: 'none', index: 0 },
        { x: 184.8, y: 313.5, radius: 0, curve: 'none', index: 1 },
      ],
    },
    {
      type: 'interiorWall',
      code: '6-n-f2-r0-int2',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        { x: 184.8, y: 355, radius: 0, curve: 'none', index: 0 },
        { x: 184.8, y: 482, radius: 0, curve: 'none', index: 1 },
      ],
    },
    {
      type: 'stairs',
      code: '6-n-f2-r0-st1',
      label: 'Stairs',
      origin: { x: 0, y: 0 },
      vertical: false,
      outline: [
        { x: 184.9, y: 228.7, index: 0 },
        { x: 217.6, y: 228.7, index: 1 },
        { x: 217.6, y: 320.5, index: 2 },
        { x: 184.9, y: 320.5, indeX: 3 },
      ],
    },
    {
      type: 'stairs',
      code: '6-n-f2-r0-st2',
      label: 'Stairs',
      origin: { x: 0, y: 0 },
      vertical: false,
      outline: [
        { x: 184.9, y: 360.2, index: 0 },
        { x: 217.6, y: 360.3, index: 1 },
        { x: 217.6, y: 482.2, index: 2 },
        { x: 184.9, y: 478.2, index: 3 },
      ],
    },
  ],
};
