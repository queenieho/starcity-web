const nbFloorOne = {
  unit: {
    building: '6-nottin',
    name: '6-nottin-f1',
    code: '6-n-f1',
    number: 1,
    scale: 1, // 1px to 1 inch
    origin: { x: 0, y: 0 },
  },
  room: {
    outline: [
      {
        x: 102.9, y: 0, radius: 0, curve: 'none', index: 0,
      },
      {
        x: 240, y: 0, radius: 0, curve: 'none', index: 1,
      },
      {
        x: 240, y: 656, radius: 0, curve: 'none', index: 2,
      },
      {
        x: 0, y: 656, radius: 0, curve: 'none', index: 3,
      },
      {
        x: 0, y: 435, radius: 0, curve: 'none', index: 4,
      },
      {
        x: 38.85, y: 435, radius: 0, curve: 'none', index: 5,
      },
      {
        x: 38.85, y: 351, radius: 0, curve: 'none', index: 6,
      },
      {
        x: 0, y: 312, radius: 0, curve: 'none', index: 7,
      },
      {
        x: 0, y: 71.3, radius: 0, curve: 'none', index: 8,
      },
      {
        x: 102.9, y: 71.3, radius: 0, curve: 'none', index: 9,
      },
    ],
  },
  features: [
    {
      type: 'window',
      code: '6-n-f1-win1',
      label: 'Window',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 113, y: 31.7, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 113, y: 64.5, radius: 0, curve: 'none', index: 1,
        },
      ],
    },
    {
      type: 'slidingDoor',
      code: '6-n-f1-slid1',
      label: 'Sliding Door',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 24.2, y: 79, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 59.7, y: 79, radius: 0, curve: 'none', index: 1,
        },
        {
          x: 95.3, y: 79, radius: 0, curve: 'none', index: 2,
        },
      ],
    },
    {
      type: 'door',
      code: '6-n-f1-d1',
      label: 'Door',
      origin: { x: 0, y: 0 },
      clockwise: true,
      outline: [
        {
          x: 162.7, y: 651.7, radius: 0, curve: 'none', index: 0,
        },
        {
          x: 198, y: 651.7, radius: 0, curve: 'none', index: 1,
        },
        {
          x: 162.7, y: 687.9, radius: 0, curve: 'none', index: 2,
        },
      ],
    },
    {
      type: 'interiorWall',
      code: '6-n-f1-int1',
      label: 'Interior Wall',
      origin: { x: 0, y: 0 },
      outline: [
        {
          x: 185.4, y: 450, radius: 0, curve: 'none', index: 1,
        },
        {
          x: 185.4, y: 522, radius: 0, curve: 'none', index: 2,
        },
      ],
    },
  ],
};
